package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.service.ShootService;
import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Shoot 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/shoot")
@Api(tags = "协拍服务")
public class ShootController {

    private final ShootService shootService;

    public ShootController(ShootService shootService) {
        this.shootService = shootService;
    }

    /**
     * 新增 Shoot 记录
     * @return 是否新增成功
     */
    @PostMapping("/add")
    @ApiOperation("新增协拍服务")
    public ApiResponse<Boolean> add(@RequestBody Shoot shoot) {
        return ApiResponse.success(shootService.save(shoot));
    }

    /**
     * 更新 Shoot 记录
     * @return 是否更新成功
     */
    @PutMapping("/update")
    @ApiOperation("修改协拍相关信息")
    public ApiResponse<Boolean> update(@RequestBody Shoot shoot) {
        return ApiResponse.success(shootService.updateById(shoot));
    }

    /**
     * 删除指定 ID 的 Shoot 记录
     * @param id 主键 ID
     * @return 是否删除成功
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除服务")
    public ApiResponse<Boolean> delete(@PathVariable("id") Integer id) {
        return ApiResponse.success(shootService.removeById(id));
    }

    /**
     * 根据 ID 获取 Shoot 详情
     * @param id 主键 ID
     * @return 匹配的实体对象
     */
    @GetMapping("/get/{id}")
    @ApiOperation("通过id查询")
    public ApiResponse<Shoot> getById(@PathVariable("id") Integer id) {
        return ApiResponse.success(shootService.getById(id));
    }

    /**
     * 获取所有 Shoot 列表（不分页）
     * @return 实体列表
     */
    @GetMapping("/get/all")
    @ApiOperation("查询所有服务")
    public ApiResponse<List<Shoot>> list() {
        return ApiResponse.success(shootService.list());
    }

    /**
     * 分页查询 Shoot 列表
     * 支持关键字模糊搜索与排序
     * @param pageRequest 分页与筛选请求参数
     * @return 分页结果
     */
    @PostMapping("/get/page")
    @ApiOperation("分页查询")
    public ApiResponse<Page<Shoot>> getPage(@RequestBody PageRequest pageRequest) {
        Page<Shoot> page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        QueryWrapper<Shoot> wrapper = new QueryWrapper<>();

        if (pageRequest.getKeyword() != null && !pageRequest.getKeyword().isEmpty()) {
            wrapper.like("name", pageRequest.getKeyword());
        }

        if (pageRequest.getSortBy() != null && !pageRequest.getSortBy().isEmpty()) {
            wrapper.orderBy(true, "asc".equalsIgnoreCase(pageRequest.getSortOrder()), pageRequest.getSortBy());
        }

        return ApiResponse.success(shootService.page(page, wrapper));
    }
}
