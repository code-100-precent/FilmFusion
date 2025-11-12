package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.service.DramaService;
import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Drama 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/drama")
@Api(tags = "影视剧")
public class DramaController {

    private final DramaService dramaService;

    public DramaController(DramaService dramaService) {
        this.dramaService = dramaService;
    }

    /**
     * 新增 Drama 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @PostMapping("/add")
    public ApiResponse<Boolean> add(@RequestBody Drama drama) {
        return ApiResponse.success(dramaService.save(drama));
    }

    /**
     * 更新 Drama 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @ApiOperation("更新影视剧信息")
    public ApiResponse<Boolean> update(@RequestBody Drama drama) {
        return ApiResponse.success(dramaService.updateById(drama));
    }

    /**
     * 删除指定 ID 的 Drama 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除信息")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(dramaService.removeById(id));
    }

    /**
     * 根据 ID 获取 Drama 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/get/{id}")
    @ApiOperation("获取信息")
    public ApiResponse<Drama> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(dramaService.getById(id));
    }

    /**
     * 获取所有 Drama 列表（不分页）
     * @return 实体列表
     */
    @GetMapping("/get/all")
    @ApiOperation("获取全部信息")
    public ApiResponse<List<Drama>> list() {
        return ApiResponse.success(dramaService.list());
    }

    /**
     * 分页查询 Drama 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @ApiOperation("分页查询")
    public ApiResponse<Page<Drama>> getPage(@RequestBody PageRequest pageRequest) {
        Page<Drama> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Drama> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword()); // 可自定义字段
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(dramaService.page(page, wrapper));
    }
}
