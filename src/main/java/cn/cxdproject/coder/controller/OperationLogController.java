package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.model.entity.OperationLog;
import cn.cxdproject.coder.service.OperationLogService;
import cn.code100.coder.common.request.PageRequest;
import cn.code100.coder.common.response.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OperationLog 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/operationlog")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 新增 OperationLog 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping
    public ApiResponse<Boolean> add(@RequestBody OperationLog entity) {
        return ApiResponse.success(operationLogService.save(entity));
    }

    /**
     * 更新 OperationLog 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody OperationLog entity) {
        return ApiResponse.success(operationLogService.updateById(entity));
    }

    /**
     * 删除指定 ID 的 OperationLog 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(operationLogService.removeById(id));
    }

    /**
     * 根据 ID 获取 OperationLog 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/{id}")
    public ApiResponse<OperationLog> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(operationLogService.getById(id));
    }

    /**
     * 获取所有 OperationLog 列表（不分页）
     * @return 实体列表
     */
    @GetMapping
    public ApiResponse<List<OperationLog>> list() {
        return ApiResponse.success(operationLogService.list());
    }

    /**
     * 分页查询 OperationLog 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ApiResponse<Page<OperationLog>> getPage(@RequestBody PageRequest pageRequest) {
        Page<OperationLog> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<OperationLog> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(operationLogService.page(page, wrapper));
    }
}
