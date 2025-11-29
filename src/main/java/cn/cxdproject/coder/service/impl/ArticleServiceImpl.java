package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateArticleDTO;
import cn.cxdproject.coder.model.dto.UpdateArticleDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.model.vo.BannerVO;
import cn.cxdproject.coder.service.ArticleService;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Article 服务实现类
 *
 * @author Hibiscus-code-generate
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    private final ArticleMapper articleMapper;
    private final Cache<String, Object> cache;
    private final RedisUtils redisUtils;

    public ArticleServiceImpl(
            ArticleMapper articleMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils
    ) {
        this.articleMapper = articleMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
    }

    @Override
    @CircuitBreaker(name = "articleGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public ArticleVO getArticleById(Long articleId) {
        Object store = cache.getIfPresent(CaffeineConstants.ARTICLE + articleId);
        if (store != null) {
            return toArticleVO((Article) store);
        } else {
            Article article = this.getById(articleId);
            if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.ARTICLE + articleId, article);
            return toArticleVO(article);
        }
    }


    @Override
    @CircuitBreaker(name = "articleGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public List<ArticleVO> getArticlePage(Long lastId, int size, String keyword) {
        List<Long> ids = articleMapper.selectIds(lastId, size, keyword);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Article> articles = articleMapper.selectBatchIds(ids);

        Map<Long, Article> articleMap = articles.stream()
                .collect(Collectors.toMap(Article::getId, a -> a));


        return ids.stream()
                .map(articleMap::get)
                .filter(Objects::nonNull)
                .map(this::toArticleVO)
                .collect(Collectors.toList());
    }

    @Override
    @Bulkhead(name = "add", type = Bulkhead.Type.SEMAPHORE)
    public ArticleVO createArticleByAdmin(CreateArticleDTO createDTO) {
        // 获取当前管理员用户
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
        }
        if (createDTO.getCover() == null) {
            createDTO.setCover(Constants.DEFAULT_COVER);
        }
        // 创建文章
        Article article = Article.builder()
                .title(createDTO.getTitle())
                .issueUnit(createDTO.getIssueUnit())
                .content(createDTO.getContent())
                .issueTime(LocalDateTime.now())
                .userId(currentUser.getId())
                .cover(createDTO.getCover())
                .image(createDTO.getImage())
                .thumbCover(createDTO.getThumbCover())
                .thumbImage(createDTO.getThumbImage())
                .build();

        // 保存文章
        this.save(article);
        return toArticleVO(article);
    }

    @Override
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    public ArticleVO updateArticleByAdmin(Long articleId, UpdateArticleDTO updateDTO) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getTitle() != null) article.setTitle(updateDTO.getTitle());
        if (updateDTO.getIssueUnit() != null) article.setIssueUnit(updateDTO.getIssueUnit());
        if (updateDTO.getContent() != null) article.setContent(updateDTO.getContent());
        if (updateDTO.getCover() != null) article.setCover(updateDTO.getCover());
        if (updateDTO.getThumbCover() != null) article.setThumbCover(updateDTO.getThumbCover());
        if (updateDTO.getThumbImage() != null) article.setThumbImage(updateDTO.getThumbImage());


        cache.asMap().put(CaffeineConstants.ARTICLE + articleId, article);
        this.updateById(article);
        return toArticleVO(article);
    }

    @Override
    @Bulkhead(name = "delete", type = Bulkhead.Type.SEMAPHORE)
    public void deleteArticleByAdmin(Long articleId) {
        boolean updated = articleMapper.update(null,
                Wrappers.<Article>lambdaUpdate()
                        .set(Article::getDeleted, true)
                        .eq(Article::getId, articleId)
                        .eq(Article::getDeleted, false)
        ) > 0;

        if (!updated) {
            Article article = this.getById(articleId);
            if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.ARTICLE + articleId);
    }


    @Override
    public ArticleVO toArticleVO(Article article) {
        if (article == null) {
            return null;
        }
        return ArticleVO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .issueUnit(article.getIssueUnit())
                .issueTime(article.getIssueTime())
                .content(article.getContent())
                .userId(article.getUserId())
                .cover(article.getCover())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .image(article.getImage())
                .thumbCover(article.getThumbCover())
                .thumbImage(article.getThumbImage())
                .build();
    }

    @Override
    public ArticleVO getByIdFallback(Long articleId,Throwable e) {
        Object store;
        store = cache.getIfPresent(CaffeineConstants.ARTICLE + articleId);
        if (store != null) {
            return toArticleVO((Article) store);
        }
        store = redisUtils.get(RedisKeyConstants.ARTICLE+articleId);
        if (store != null) {
            Article article = JsonUtils.fromJson((String) store, Article.class);
            return toArticleVO(article);
        }
        return null;
    }

    @Override
    public List<ArticleVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        try {
            // 从 Redis 获取缓存的全量文章（假设是 ArticleVO[] 的 JSON）
            String json = (String) redisUtils.get(TaskConstants.ARTICLE);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            ArticleVO[] array = JsonUtils.fromJson(json, ArticleVO[].class);
            if (array == null || array.length == 0) {
                return Collections.emptyList();
            }

            // 直接取前 N 条（N = min(size, 缓存长度)）
            // 注意：这里忽略 lastId 和 keyword，因为 fallback 只提供静态兜底数据
            int take = Math.min(size, array.length);
            return new ArrayList<>(Arrays.asList(array).subList(0, take));

        } catch (Exception ex) {
            log.error("Fallback failed", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Page<ArticleVO> getArticlePagAdmine(Page<Article> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Article> articles = articleMapper.getAdminPage(keyword, offset, size);

        List<ArticleVO> voList = articles.stream()
                .map(this::toArticleVO)
                .collect(Collectors.toList());

        return new Page<ArticleVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }
}
