package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.model.entity.Feedback;
import cn.cxdproject.coder.service.FeedbackService;
import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feedback 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /**
     * 新增 Feedback 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping
    public ApiResponse<Boolean> add(@RequestBody Feedback entity) {
        return ApiResponse.success(feedbackService.save(entity));
    }

    /**
     * 更新 Feedback 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody Feedback entity) {
        return ApiResponse.success(feedbackService.updateById(entity));
    }

    /**
     * 删除指定 ID 的 Feedback 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(feedbackService.removeById(id));
    }

    /**
     * 根据 ID 获取 Feedback 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/{id}")
    public ApiResponse<Feedback> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(feedbackService.getById(id));
    }

    /**
     * 获取所有 Feedback 列表（不分页）
     * @return 实体列表
     */
    @GetMapping
    public ApiResponse<List<Feedback>> list() {
        return ApiResponse.success(feedbackService.list());
    }

    /**
     * 分页查询 Feedback 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ApiResponse<Page<Feedback>> getPage(@RequestBody PageRequest pageRequest) {
        Page<Feedback> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Feedback> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(feedbackService.page(page, wrapper));
    }
}
