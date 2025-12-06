package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.common.enums.LogType;
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
    @Loggable(
            type = LogType.ARTICLE_GET,
            value = "Get article by ID: #{#articleId}"
    )
    public ArticleVO getArticleById(Long articleId) {
        Object store = cache.getIfPresent(CaffeineConstants.ARTICLE + articleId);
        if (store != null) {
            return toArticleVO((Article) store);
        } else {
            Article article = this.getById(articleId);
            if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.put(CaffeineConstants.ARTICLE + articleId, article);
            return toArticleVO(article);
        }
    }


    @Override
    @CircuitBreaker(name = "articleGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    @Loggable(
            type = LogType.ARTICLE_USER_GET_PAGE,
            value = "User get page "
    )
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
    @Loggable(
            type = LogType.ARTICLE_CREATE,
            value = "Create article "
    )
    public ArticleVO createArticleByAdmin(CreateArticleDTO createDTO) {
        // 获取当前管理员用户
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
        }
        if (createDTO.getImage() == null) {
            createDTO.setImage(Constants.DEFAULT_COVER);
            createDTO.setThumbImage(Constants.DEFAULT_THUMB_COVER);
        }
        // 创建文章
        Article article = Article.builder()
                .title(createDTO.getTitle())
                .issueUnit(createDTO.getIssueUnit())
                .content(createDTO.getContent())
                .issueTime(LocalDateTime.now())
                .userId(currentUser.getId())
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .build();

        // 保存文章
        this.save(article);
        return toArticleVO(article);
    }

    @Override
    @Loggable(
            type = LogType.ARTICLE_UPDATE,
            value = "update article ID: #{#articleId}"
    )
    public ArticleVO updateArticleByAdmin(Long articleId, UpdateArticleDTO updateDTO) {
        // 1. 先校验文章是否存在且未删除（必须做，用于抛出 NotFoundException）
        Article existing = this.getById(articleId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 2. 执行动态更新（只更新非空字段）
        int updated = articleMapper.updateArticle(articleId,updateDTO);

        // 3. 如果更新影响行数为 0，说明条件不满足（比如已被删除），也可视为未找到
        if (updated == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 4. 重新加载最新数据
        Article updatedArticle = this.getById(articleId);

        // 5. 更新缓存
        cache.put(CaffeineConstants.ARTICLE + articleId, updatedArticle);

        // 6. 返回 VO
        return toArticleVO(updatedArticle);
    }

    @Override
    @Loggable(
            type = LogType.ARTICLE_DELETE,
            value = "Delete article ID: #{#articleId}"
    )
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
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .image(article.getImage())
                .thumbImage(article.getThumbImage())
                .build();
    }

    @Override
    public ArticleVO getByIdFallback(Long articleId,Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        Object store = cache.getIfPresent(CaffeineConstants.ARTICLE + articleId);
        if (store != null) {
            return toArticleVO((Article) store);
        }

        return null;
    }

    @Override
    public List<ArticleVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

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

            int take = Math.min(size, array.length);
            return new ArrayList<>(Arrays.asList(array).subList(0, take));

        } catch (Exception ex) {
            log.error("Fallback failed", ex);
            return Collections.emptyList();
        }
    }

    @Override
    @Loggable(
            type = LogType.ARTICLE_ADMIN_GET_PAGE,
            value = "Admin get article page"
    )
    public Page<ArticleVO> getArticlePagAdmin(Page<Article> page, String keyword) {

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
