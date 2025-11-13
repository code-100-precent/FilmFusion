package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.service.LocationService;
import cn.code100.coder.common.request.PageRequest;
import cn.code100.coder.common.response.ApiResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Location 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * 新增 Location 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping
    public ApiResponse<Boolean> add(@RequestBody Location entity) {
        return ApiResponse.success(locationService.save(entity));
    }

    /**
     * 更新 Location 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody Location entity) {
        return ApiResponse.success(locationService.updateById(entity));
    }

    /**
     * 删除指定 ID 的 Location 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(locationService.removeById(id));
    }

    /**
     * 根据 ID 获取 Location 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/{id}")
    public ApiResponse<Location> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(locationService.getById(id));
    }

    /**
     * 获取所有 Location 列表（不分页）
     * @return 实体列表
     */
    @GetMapping
    public ApiResponse<List<Location>> list() {
        return ApiResponse.success(locationService.list());
    }

    /**
     * 分页查询 Location 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    public ApiResponse<Page<Location>> getPage(@RequestBody PageRequest pageRequest) {
        Page<Location> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Location> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(locationService.page(page, wrapper));
    }
}
