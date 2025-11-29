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
 *
 * @author Hibiscus-code-generate
 */
public interface ArticleService extends IService<Article> {


    /**
     * 获取文章详情（公开接口）
     *
     * @param articleId 文章ID
     * @return 文章VO
     */
    ArticleVO getArticleById(Long articleId);

    /**
     * 分页获取文章列表（按时间倒序，公开接口）
     *
     * @param page    分页对象
     * @param keyword 关键字（搜索标题或内容）
     * @return 分页结果
     */
   List<ArticleVO> getArticlePage(Long lastId,int size, String keyword);

    /**
     * 管理员创建文章
     * @param createDTO 创建文章DTO
     * @return 文章VO
     */
    ArticleVO createArticleByAdmin(CreateArticleDTO createDTO);

    /**
     * 管理员更新文章
     *
     * @param articleId 文章ID
     * @param updateDTO 更新文章DTO
     * @return 文章VO
     */
    ArticleVO updateArticleByAdmin(Long articleId, UpdateArticleDTO updateDTO);

    /**
     * 管理员删除文章（逻辑删除）
     *
     * @param articleId 文章ID
     */
    void deleteArticleByAdmin(Long articleId);


    /**
     * 将Article实体转换为ArticleVO
     *
     * @param article 文章实体
     * @return 文章VO
     */
    ArticleVO toArticleVO(Article article);

    ArticleVO getByIdFallback(Long id,Throwable e);

    List<ArticleVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    Page<ArticleVO> getArticlePagAdmine(Page<Article> page, String keyword);
}
