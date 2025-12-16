package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateArticleDTO;
import cn.cxdproject.coder.model.dto.UpdateArticleDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.vo.ArticleVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Article 服务接口
 * 提供文章的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface ArticleService extends IService<Article> {


    /**
     * 获取文章详情（公开接口）
     */
    ArticleVO getArticleById(Long articleId);

    /**
     * 分页获取文章列表（按时间倒序，公开接口）
     */
   List<ArticleVO> getArticlePage(Long lastId, int size, String keyword);

    /**
     * 管理员创建文章
     */
    ArticleVO createArticleByAdmin(CreateArticleDTO createDTO);

    /**
     * 管理员更新文章
     */
    ArticleVO updateArticleByAdmin(Long articleId, UpdateArticleDTO updateDTO);

    /**
     * 管理员删除文章（逻辑删除）
     */
    void deleteArticleByAdmin(Long articleId);


    /**
     * 将Article实体转换为ArticleVO
     */
    ArticleVO toArticleVO(Article article);

    /**
     * 获取文章详情的降级方法
     */
    ArticleVO getByIdFallback(Long id, Throwable e);

    /**
     * 分页查询文章列表的降级方法
     */
    List<ArticleVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    /**
     * 管理员分页查询文章
     */
    Page<ArticleVO> getArticlePagAdmin(Page<Article> page, String keyword);
}
