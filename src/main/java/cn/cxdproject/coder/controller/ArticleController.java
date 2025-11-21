package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateArticleDTO;
import cn.cxdproject.coder.model.dto.UpdateArticleDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.service.ArticleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 文章控制器
 * 
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/article")
@Validated
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // ==================== 公开接口 ====================

    /**
     * 获取文章详情（公开）
     */
    @GetMapping("/{id}")
    @PublicAccess
    @CircuitBreaker(name = "articlePage", fallbackMethod = "getByIdFallback")
    public ApiResponse<ArticleVO> getArticleById(@PathVariable @NotNull(message = "文章ID不能为空") Long id) {
        ArticleVO articleVO = articleService.getArticleById(id);
        return ApiResponse.success(articleVO);
    }

    /**
     * 分页获取文章列表（按时间倒序，公开）
     */
    @GetMapping("/page")
    @PublicAccess

    public PageResponse<ArticleVO> getArticlePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Article> page = new Page<>(current, size);
        Page<ArticleVO> articlePage = articleService.getArticlePage(page, keyword);
        return PageResponse.of(
                (int) articlePage.getCurrent(),
                (int) articlePage.getSize(),
                articlePage.getTotal(),
                articlePage.getRecords()
        );
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员创建文章
     */
    @PostMapping("/admin/create")
    @Bulkhead(name = "add", type = Bulkhead.Type.SEMAPHORE)
    public ApiResponse<ArticleVO> createArticleByAdmin(@Valid @RequestBody CreateArticleDTO createDTO) {
        ArticleVO articleVO = articleService.createArticleByAdmin(createDTO);
        return ApiResponse.success(articleVO);
    }

    /**
     * 管理员更新文章
     */
    @PutMapping("/admin/update/{id}")
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    public ApiResponse<ArticleVO> updateArticleByAdmin(
            @PathVariable @NotNull(message = "文章ID不能为空") Long id,
            @Valid @RequestBody UpdateArticleDTO updateDTO) {
        // 权限检查在拦截器中完成
        ArticleVO articleVO = articleService.updateArticleByAdmin(id, updateDTO);
        return ApiResponse.success(articleVO);
    }

    /**
     * 管理员删除文章
     */
    @DeleteMapping("/admin/delete/{id}")
    @Bulkhead(name = "delete", type = Bulkhead.Type.SEMAPHORE)
    public ApiResponse<Void> deleteArticleByAdmin(@PathVariable @NotNull(message = "文章ID不能为空") Long id) {
        // 权限检查在拦截器中完成
        articleService.deleteArticleByAdmin(id);
        return ApiResponse.success();
    }

    //根据id查询降级
    public ApiResponse<ArticleVO> getByIdFallback(Long id) {
        ArticleVO articleVO = articleService.getByIdFallBack(id);
        return ApiResponse.success(articleVO);
    }

}
