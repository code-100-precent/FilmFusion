package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateArticleDTO;
import cn.cxdproject.coder.model.dto.UpdateArticleDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.service.ArticleService;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
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
    private final RedisTemplate redisTemplate;
    private final RedisUtils redisUtils;

    public ArticleServiceImpl(
            ArticleMapper articleMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisTemplate redisTemplate, RedisUtils redisUtils
    ) {
        this.articleMapper = articleMapper;
        this.cache = cache;
        this.redisTemplate = redisTemplate;
        this.redisUtils = redisUtils;
    }

    @Override
    @CircuitBreaker(name = "get", fallbackMethod = "getByIdFallback")
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
    @CircuitBreaker(name = "get", fallbackMethod = "getPageFallback")
    public Page<ArticleVO> getArticlePage(Page<Article> page, String keyword) {

        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Long> ids = articleMapper.getPageArticleIds(keyword, offset, size);
        long total = articleMapper.countByKeyword(keyword);

        if (ids.isEmpty()) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 1. 批量从 Redis 获取缓存
        List<String> keys = ids.stream().map(id -> RedisKeyConstants.ARTICLE + id).collect(Collectors.toList());
        List<String> cachedJsons = redisTemplate.opsForValue().multiGet(keys);

        // 2. 构建 Article 列表：优先用缓存，缺失的记录 ID
        List<Article> articles = new ArrayList<>(Collections.nCopies(ids.size(), null));
        List<Long> missingIds = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            String json = cachedJsons.get(i);
            if (json != null) {
                try {
                    articles.set(i, JsonUtils.fromJson(json, Article.class));
                } catch (Exception e) {
                    missingIds.add(ids.get(i));
                }
            } else {
                missingIds.add(ids.get(i));
            }
        }

        if (!missingIds.isEmpty()) {
            List<Article> dbArticles = articleMapper.selectBatchIds(missingIds);
            Map<Long, Article> dbMap = dbArticles.stream()
                    .peek(article -> {
                        // 回填 Redis 缓存
                        redisTemplate.opsForValue().set(
                                RedisKeyConstants.ARTICLE + article.getId(),
                                JsonUtils.toJson(article),
                                Duration.ofMinutes(30)
                        );
                        cache.put(CaffeineConstants.ARTICLE + article.getId(), article);
                    })
                    .collect(Collectors.toMap(Article::getId, a -> a));

            // 填回原位置
            for (int i = 0; i < ids.size(); i++) {
                if (articles.get(i) == null) {
                    articles.set(i, dbMap.get(ids.get(i)));
                }
            }
        }

        List<ArticleVO> voList = articles.stream()
                .map(this::toArticleVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Page<ArticleVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);
    }

    @Override
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
                .build();

        // 保存文章
        this.save(article);
        return toArticleVO(article);
    }

    @Override
    public ArticleVO updateArticleByAdmin(Long articleId, UpdateArticleDTO updateDTO) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getTitle() != null) {
            article.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getIssueUnit() != null) {
            article.setIssueUnit(updateDTO.getIssueUnit());
        }
        if (updateDTO.getContent() != null) {
            article.setContent(updateDTO.getContent());
        }
        if (updateDTO.getCover() != null) {
            article.setCover(updateDTO.getCover());
        }
        if (updateDTO.getImage() != null) {
            article.setImage(updateDTO.getImage());
        }

        cache.asMap().put(CaffeineConstants.ARTICLE + articleId, article);
        this.updateById(article);
        return toArticleVO(article);
    }

    @Override
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
                .build();
    }

    @Override
    public ArticleVO getByIdFallBack(Long articleId) {
        String key = CaffeineConstants.ARTICLE + articleId;
        Object store;
        store = cache.getIfPresent(key);
        if (store != null) {
            return toArticleVO((Article) store);
        }
        store = redisUtils.get(key);
        if (store != null) {
            Article article = JsonUtils.fromJson((String) store, Article.class);
            return toArticleVO(article);
        }
        return null;
    }

    @Override
    public List<ArticleVO> getPageFallback(Page<Article> page, String keyword, Throwable e) {
        try {
            String json = (String) redisUtils.get(TaskConstants.ARTICLE);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            // 使用数组方式反序列化
            ArticleVO[] array = JsonUtils.fromJson(json, ArticleVO[].class);
            if (array == null) {
                return Collections.emptyList();
            }

            return new ArrayList<>(Arrays.asList(array));

        } catch (Exception ex) {
            log.error("fallback 反序列化失败", ex);
            return Collections.emptyList();
        }
    }
}
