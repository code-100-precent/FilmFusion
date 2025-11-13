package cn.cxdproject.coder.service.impl;

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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Article 服务实现类
 * @author Hibiscus-code-generate
 */
@Slf4j
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO createArticle(Long userId, CreateArticleDTO createDTO) {
        // 创建文章
        Article article = Article.builder()
                .title(createDTO.getTitle())
                .issueUnit(createDTO.getIssueUnit())
                .content(createDTO.getContent())
                .issueTime(LocalDateTime.now())
                .userId(userId)
                .build();

        // 保存文章
        this.save(article);

        return toArticleVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO updateArticle(Long userId, Long articleId, UpdateArticleDTO updateDTO) {
        // 获取文章
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "文章不存在");
        }

        // 检查权限：只能更新自己的文章
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权修改他人的文章");
        }

        // 更新文章
        if (updateDTO.getTitle() != null) {
            article.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getIssueUnit() != null) {
            article.setIssueUnit(updateDTO.getIssueUnit());
        }
        if (updateDTO.getContent() != null) {
            article.setContent(updateDTO.getContent());
        }

        // 保存更新
        this.updateById(article);

        return toArticleVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long userId, Long articleId) {
        // 获取文章
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "文章不存在");
        }

        // 检查权限：只能删除自己的文章
        if (!article.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权删除他人的文章");
        }

        // 逻辑删除
        article.setDeleted(true);
        this.updateById(article);
    }

    @Override
    public ArticleVO getArticleById(Long articleId) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "文章不存在");
        }
        return toArticleVO(article);
    }

    @Override
    public Page<ArticleVO> getArticlePage(Page<Article> page, String keyword) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        // 关键字搜索（标题或内容）
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword));
        }

        // 按发布时间倒序（最新的在前）
        wrapper.orderByDesc("issue_time");

        // 分页查询
        Page<Article> articlePage = this.page(page, wrapper);

        // 转换为VO
        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        List<ArticleVO> voList = articlePage.getRecords().stream()
                .map(this::toArticleVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO createArticleByAdmin(CreateArticleDTO createDTO) {
        // 获取当前管理员用户
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
        }

        // 创建文章
        Article article = Article.builder()
                .title(createDTO.getTitle())
                .issueUnit(createDTO.getIssueUnit())
                .content(createDTO.getContent())
                .issueTime(LocalDateTime.now())
                .userId(currentUser.getId())
                .build();

        // 保存文章
        this.save(article);

        return toArticleVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO updateArticleByAdmin(Long articleId, UpdateArticleDTO updateDTO) {
        // 获取文章
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "文章不存在");
        }

        // 更新文章
        if (updateDTO.getTitle() != null) {
            article.setTitle(updateDTO.getTitle());
        }
        if (updateDTO.getIssueUnit() != null) {
            article.setIssueUnit(updateDTO.getIssueUnit());
        }
        if (updateDTO.getContent() != null) {
            article.setContent(updateDTO.getContent());
        }

        // 保存更新
        this.updateById(article);

        return toArticleVO(article);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticleByAdmin(Long articleId) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "文章不存在");
        }

        // 逻辑删除
        article.setDeleted(true);
        this.updateById(article);
    }

    @Override
    public Page<ArticleVO> getArticlePageByAdmin(Page<Article> page, String keyword) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        // 关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword).or().like("content", keyword).or().like("issue_unit", keyword));
        }

        // 按发布时间倒序
        wrapper.orderByDesc("issue_time");

        // 分页查询
        Page<Article> articlePage = this.page(page, wrapper);

        // 转换为VO
        Page<ArticleVO> voPage = new Page<>(articlePage.getCurrent(), articlePage.getSize(), articlePage.getTotal());
        List<ArticleVO> voList = articlePage.getRecords().stream()
                .map(this::toArticleVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public ArticleVO getArticleByIdByAdmin(Long articleId) {
        Article article = this.getById(articleId);
        if (article == null || Boolean.TRUE.equals(article.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "文章不存在");
        }
        return toArticleVO(article);
    }

    @Override
    public ArticleVO toArticleVO(Article article) {
        if (article == null) {
            return null;
        }
        ArticleVO vo = new ArticleVO();
        BeanUtils.copyProperties(article, vo);
        return vo;
    }
}
