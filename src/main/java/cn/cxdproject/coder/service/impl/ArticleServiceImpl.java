package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.ArticleConstants;
import cn.cxdproject.coder.common.constants.CaffeineConstants;
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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public ArticleVO createArticle(Long userId, CreateArticleDTO createDTO) {
        if(createDTO.getCover()==null){
            createDTO.setCover("https://auto-avatar.oss-cn-beijing.aliyuncs.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251115152833_120_8.jpg");
        }
        // 创建文章
        Article article = Article.builder()
                .title(createDTO.getTitle())
                .issueUnit(createDTO.getIssueUnit())
                .content(createDTO.getContent())
                .issueTime(LocalDateTime.now())
                .userId(userId)
                .cover(createDTO.getCover())
                .image(createDTO.getImage())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        // 保存文章
        this.save(article);
        return toArticleVO(article);
    }

    @Override
    public ArticleVO updateArticle(Long userId, Long articleId, UpdateArticleDTO updateDTO) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ArticleConstants.NOT_FIND);
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), ArticleConstants.NO_PERMISSION);
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
        article.setUpdatedAt(LocalDateTime.now());
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
                throw new NotFoundException(NOT_FOUND.code(), ArticleConstants.NOT_FIND);
            } else {
                throw new BusinessException(FORBIDDEN.code(), ArticleConstants.NO_PERMISSION);
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
                throw new NotFoundException(NOT_FOUND.code(), ArticleConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.ARTICLE + articleId, article);
            return toArticleVO(article);
        }
    }


    @Override
    public Page<ArticleVO> getArticlePage(Page<Article> page, String keyword) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        wrapper.select(ArticleConstants.ID, ArticleConstants.TITLE, ArticleConstants.ISSUE_NAME,ArticleConstants.ISSUE_TIME , ArticleConstants.USER_ID, ArticleConstants.COVER)
                .eq(ArticleConstants.DELETED, false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword));
        }

        wrapper.orderByDesc("issue_time");

        Page<Article> articlePage = this.page(page, wrapper);

        List<ArticleVO> voList = articlePage.getRecords().stream()
                .map(article -> new ArticleVO(
                        article.getId(),
                        article.getTitle(),
                        article.getIssueUnit(),
                        article.getIssueTime(),
                        article.getUserId(),
                        article.getCover()
                ))
                .collect(Collectors.toList());

        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

//    @Override
//    public ArticleVO createArticleByAdmin(CreateArticleDTO createDTO) {
//        // 获取当前管理员用户
//        User currentUser = AuthContext.getCurrentUser();
//        if (currentUser == null) {
//            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
//        }
//        if (currentUser.getRole()== "user"){
//            throw new BusinessException(FORBIDDEN.code(), "无权创建文章");
//        }
//
//        // 创建文章
//        Article article = Article.builder()
//                .title(createDTO.getTitle())
//                .issueUnit(createDTO.getIssueUnit())
//                .content(createDTO.getContent())
//                .issueTime(LocalDateTime.now())
//                .userId(currentUser.getId())
//                .build();
//
//        // 保存文章
//        this.save(article);
//
//        return toArticleVO(article);
//    }

    @Override
    public ArticleVO updateArticleByAdmin(Long articleId, UpdateArticleDTO updateDTO) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ArticleConstants.NOT_FIND);
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
                throw new NotFoundException(NOT_FOUND.code(), ArticleConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.ARTICLE+articleId);
    }

//    @Override
//    public Page<ArticleVO> getArticlePageByAdmin(Page<Article> page, String keyword) {
//        QueryWrapper<Article> wrapper = new QueryWrapper<>();
//
//        wrapper.select("id", "title", "issue_unit", "issue_time", "user_id", "cover")
//                .eq("deleted", false);
//
//        if (keyword != null && !keyword.isEmpty()) {
//            wrapper.and(w -> w.like("title", keyword));
//        }
//
//        wrapper.orderByDesc("issue_time");
//
//        Page<Article> articlePage = this.page(page, wrapper);
//
//        List<ArticleVO> voList = articlePage.getRecords().stream()
//                .map(article -> new ArticleVO(
//                        article.getId(),
//                        article.getTitle(),
//                        article.getIssueUnit(),
//                        article.getIssueTime(),
//                        article.getUserId(),
//                        article.getCover()
//                ))
//                .collect(Collectors.toList());
//
//        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
//        voPage.setRecords(voList);
//
//        return voPage;
//    }

//    @Override
//    public ArticleVO getArticleByIdByAdmin(Long articleId) {
//        Article article = this.getById(articleId);
//        if (article == null) {
//            throw new NotFoundException(NOT_FOUND.code(), "文章不存在");
//        }
//        return toArticleVO(article);
//    }

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
