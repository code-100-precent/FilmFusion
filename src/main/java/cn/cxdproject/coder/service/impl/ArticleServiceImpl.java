package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.constants.Constants;
import cn.cxdproject.coder.common.constants.ResponseConstants;
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
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

    public ArticleServiceImpl(
            ArticleMapper articleMapper,
            @Qualifier("cache") Cache<String, Object> cache) {
        this.articleMapper = articleMapper;
        this.cache = cache;
    }


    @Override
    public ArticleVO updateArticle(Long userId, Long articleId, UpdateArticleDTO updateDTO) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
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
        if(updateDTO.getCover() != null){
            article.setCover(updateDTO.getCover());
        }
        if(updateDTO.getImage() != null){
            article.setImage(updateDTO.getImage());
        }

        cache.asMap().put(CaffeineConstants.ARTICLE + articleId, article);
        this.updateById(article);
        return toArticleVO(article);
    }

    @Override
    public void deleteArticle(Long userId, Long articleId) {
       boolean updated = articleMapper.update(null,
                Wrappers.<Article>lambdaUpdate()
                        .set(Article::getDeleted, true)
                        .eq(Article::getId, articleId)
                        .eq(Article::getUserId, userId)
                        .eq(Article::getDeleted, false)
        ) > 0;

        if (!updated) {
            Article article = this.getById(articleId);
            if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            } else {
                throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
            }
        }
        cache.invalidate(CaffeineConstants.ARTICLE+articleId);
    }

    @Override
    public ArticleVO getArticleById(Long articleId) {
        Object store = cache.asMap().get(CaffeineConstants.ARTICLE + articleId);
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
    public Page<ArticleVO> getArticlePage(Page<Article> page, String keyword) {
        long current = page.getCurrent(); // MyBatis-Plus 页码从 1 开始
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Article> articles = articleMapper.getPage(keyword, offset, size);

        List<ArticleVO> voList = articles.stream()
                .map(article -> new ArticleVO(
                        article.getId(),
                        article.getTitle(),
                        article.getIssueUnit(),
                        article.getIssueTime(),
                        article.getUserId(),
                        article.getCover()
                ))
                .collect(Collectors.toList());

        return new Page<ArticleVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }

    @Override
    public ArticleVO createArticleByAdmin(CreateArticleDTO createDTO) {
        // 获取当前管理员用户
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
        }
        if(createDTO.getCover()==null){
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
        if(updateDTO.getCover() != null){
            article.setCover(updateDTO.getCover());
        }
        if(updateDTO.getImage() != null){
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
        cache.invalidate(CaffeineConstants.ARTICLE+articleId);
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

}
