package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.service.ShootService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 协拍服务控制器
 * 
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/shoot")
@Validated
public class ShootController {

    private final ShootService shootService;

    public ShootController(ShootService shootService) {
        this.shootService = shootService;
    }

    // ==================== 公开接口 ====================

    /**
     * 获取协拍服务详情（公开）
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<ShootVO> getShootById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        ShootVO shootVO = shootService.getShootById(id);
        return ApiResponse.success(shootVO);
    }

    /**
     * 分页获取协拍服务列表（按时间倒序，公开，只显示上线的）
     */
    @GetMapping("/page")
    @PublicAccess
    public PageResponse<ShootVO> getShootPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Shoot> page = new Page<>(current, size);
        Page<ShootVO> shootPage = shootService.getShootPage(page, keyword);
        return PageResponse.of(
                (int) shootPage.getCurrent(),
                (int) shootPage.getSize(),
                shootPage.getTotal(),
                shootPage.getRecords()
        );
    }

    // ==================== 普通用户接口 ====================

    /**
     * 创建协拍服务
     */
    @PostMapping
    public ApiResponse<ShootVO> createShoot(@Valid @RequestBody CreateShootDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        ShootVO shootVO = shootService.createShoot(currentUser.getId(), createDTO);
        return ApiResponse.success(shootVO);
    }

    /**
     * 更新协拍服务（只能更新自己的）
     */
    @PutMapping("/{id}")
    public ApiResponse<ShootVO> updateShoot(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateShootDTO updateDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        ShootVO shootVO = shootService.updateShoot(currentUser.getId(), id, updateDTO);
        return ApiResponse.success(shootVO);
    }

    /**
     * 删除协拍服务（只能删除自己的）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteShoot(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        shootService.deleteShoot(currentUser.getId(), id);
        return ApiResponse.success();
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员创建协拍服务
     */
    @PostMapping("/admin/create")
    public ApiResponse<ShootVO> createShootByAdmin(@Valid @RequestBody CreateShootDTO createDTO) {
        ShootVO shootVO = shootService.createShootByAdmin(createDTO);
        return ApiResponse.success(shootVO);
    }

    /**
     * 管理员更新协拍服务
     */
    @PutMapping("/admin/{id}")
    public ApiResponse<ShootVO> updateShootByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateShootDTO updateDTO) {
        ShootVO shootVO = shootService.updateShootByAdmin(id, updateDTO);
        return ApiResponse.success(shootVO);
    }

    /**
     * 管理员删除协拍服务
     */
    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deleteShootByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        shootService.deleteShootByAdmin(id);
        return ApiResponse.success();
    }

    /**
     * 管理员分页查询协拍服务
     */
    @GetMapping("/admin/page")
    public PageResponse<ShootVO> getShootPageByAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Shoot> page = new Page<>(current, size);
        Page<ShootVO> shootPage = shootService.getShootPageByAdmin(page, keyword);
        return PageResponse.of(
                (int) shootPage.getCurrent(),
                (int) shootPage.getSize(),
                shootPage.getTotal(),
                shootPage.getRecords()
        );
    }

    /**
     * 管理员获取协拍服务详情
     */
    @GetMapping("/admin/{id}")
    public ApiResponse<ShootVO> getShootByIdByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        ShootVO shootVO = shootService.getShootByIdByAdmin(id);
        return ApiResponse.success(shootVO);
    }
}
