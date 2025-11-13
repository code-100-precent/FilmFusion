package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageRequest;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.service.ArticleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Article 控制器，提供资讯文章相关接口
 * 包括：文章列表查询（包含摘要）、文章详情查看、管理员文章管理
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/article")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 获取文章列表（包含摘要信息）
     * 用于前端展示文章列表，只返回标题、发布单位、发布时间等基本信息
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果（包含文章摘要信息）
     */
    @PostMapping("/list")
    public ApiResponse<Page<Article>> getArticleList(@RequestBody PageRequest pageRequest) {
        Page<Article> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        
        // 只查询未删除的文章
        wrapper.eq("deleted", false);
        
        // 按发布时间倒序排列
        wrapper.orderByDesc("issue_time");
        
        // 支持按标题模糊搜索
        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("title", pageRequest.getKeyword());
        }

        return ApiResponse.success(articleService.page(page, wrapper));
    }

    /**
     * 获取文章详情（完整信息）
     * 用于查看文章的完整内容
     * @param id 文章ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Article> getArticleDetail(@PathVariable("id") Long id) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("id", id).eq("deleted", false);
        Article article = articleService.getOne(wrapper);
        
        if (article == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        
        return ApiResponse.success(article);
    }

    /**
     * 管理员创建文章
     * @param article 文章信息
     * @return 创建结果
     */
    @PostMapping("/admin")
    public ApiResponse<Boolean> createArticle(@RequestBody Article article) {
        // 设置发布时间为当前时间
        article.setIssueTime(LocalDateTime.now());
        
        boolean result = articleService.save(article);
        if (result) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(500, "文章创建失败");
        }
    }

    /**
     * 管理员更新文章
     * @param article 文章信息（必须包含主键ID）
     * @return 更新结果
     */
    @PutMapping("/admin")
    public ApiResponse<Boolean> updateArticle(@RequestBody Article article) {
        boolean result = articleService.updateById(article);
        if (result) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(500, "文章更新失败");
        }
    }

    /**
     * 管理员删除文章（逻辑删除）
     * @param id 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/admin/{id}")
    public ApiResponse<Boolean> deleteArticle(@PathVariable("id") Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        
        // 逻辑删除，设置deleted字段为true
        article.setDeleted(true);
        boolean result = articleService.updateById(article);
        
        if (result) {
            return ApiResponse.success(true);
        } else {
            return ApiResponse.error(500, "文章删除失败");
        }
    }

    /**
     * 管理员获取文章列表（包含已删除的文章）
     * 用于后台管理界面
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果（包含所有文章）
     */
    @PostMapping("/admin/list")
    public ApiResponse<Page<Article>> getAdminArticleList(@RequestBody PageRequest pageRequest) {
        Page<Article> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        
        // 按更新时间倒序排列
        wrapper.orderByDesc("updated_at");
        
        // 支持按标题模糊搜索
        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("title", pageRequest.getKeyword());
        }

        return ApiResponse.success(articleService.page(page, wrapper));
    }

    /**
     * 获取最新文章（用于首页展示）
     * @param limit 限制数量
     * @return 最新文章列表
     */
    @GetMapping("/latest")
    public ApiResponse<List<Article>> getLatestArticles(@RequestParam(defaultValue = "5") int limit) {
        QueryWrapper<Article> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false)
               .orderByDesc("issue_time")
               .last("LIMIT " + limit);
        
        return ApiResponse.success(articleService.list(wrapper));
    }
}
