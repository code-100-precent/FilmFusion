package ${packageName}.controller;

import ${packageName}.model.entity.${className};
import ${packageName}.service.${className}Service;
import ${packageName}.common.request.PageRequest;
import ${packageName}.common.response.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ${className} 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/${className?lower_case}")
public class ${className}Controller {

    private final ${className}Service ${className?uncap_first}Service;

    public ${className}Controller(${className}Service ${className?uncap_first}Service) {
        this.${className?uncap_first}Service = ${className?uncap_first}Service;
    }

    /**
     * 新增 ${className} 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping
    public ApiResponse<Boolean> add(@RequestBody ${className} entity) {
        return ApiResponse.success(${className?uncap_first}Service.save(entity));
    }

    /**
     * 更新 ${className} 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody ${className} entity) {
        return ApiResponse.success(${className?uncap_first}Service.updateById(entity));
    }

    /**
     * 删除指定 ID 的 ${className} 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(${className?uncap_first}Service.removeById(id));
    }

    /**
     * 根据 ID 获取 ${className} 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/{id}")
    public ApiResponse<${className}> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(${className?uncap_first}Service.getById(id));
    }

    /**
     * 获取所有 ${className} 列表（不分页）
     * @return 实体列表
     */
    @GetMapping
    public ApiResponse<List<${className}>> list() {
        return ApiResponse.success(${className?uncap_first}Service.list());
    }

    /**
     * 分页查询 ${className} 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ApiResponse<Page<${className}>> getPage(@RequestBody PageRequest pageRequest) {
        Page<${className}> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<${className}> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(${className?uncap_first}Service.page(page, wrapper));
    }
}
