package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageRequest;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.service.ArticleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Article 控制器，提供基础增删改查接口
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
     * 新增 Article 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping
    public ApiResponse<Boolean> add(@RequestBody Article entity) {
        return ApiResponse.success(articleService.save(entity));
    }

    /**
     * 更新 Article 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody Article entity) {
        return ApiResponse.success(articleService.updateById(entity));
    }

    /**
     * 删除指定 ID 的 Article 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(articleService.removeById(id));
    }

    /**
     * 根据 ID 获取 Article 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/{id}")
    public ApiResponse<Article> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(articleService.getById(id));
    }

    /**
     * 获取所有 Article 列表（不分页）
     * @return 实体列表
     */
    @GetMapping
    public ApiResponse<List<Article>> list() {
        return ApiResponse.success(articleService.list());
    }

    /**
     * 分页查询 Article 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ApiResponse<Page<Article>> getPage(@RequestBody PageRequest pageRequest) {
        Page<Article> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Article> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(articleService.page(page, wrapper));
    }
}
