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
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
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

    // ==================== 管理员接口 ====================

    /**
     * 创建协拍服务
     */
    @PostMapping("/admin/create")
    public ApiResponse<ShootVO> createShoot(@Valid @RequestBody CreateShootDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        ShootVO shootVO = shootService.createShootByAdmin(currentUser.getId(), createDTO);
        return ApiResponse.success(shootVO);
    }


    /**
     * 管理员更新协拍服务
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<ShootVO> updateShootByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateShootDTO updateDTO) {
        ShootVO shootVO = shootService.updateShootByAdmin(id, updateDTO);
        return ApiResponse.success(shootVO);
    }

    /**
     * 管理员删除协拍服务
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteShootByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        shootService.deleteShootByAdmin(id);
        return ApiResponse.success();
    }

}
