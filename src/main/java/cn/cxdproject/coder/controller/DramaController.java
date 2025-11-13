package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.service.DramaService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 电视剧备案控制器
 * 
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/drama")
@Validated
public class DramaController {

    private final DramaService dramaService;

    public DramaController(DramaService dramaService) {
        this.dramaService = dramaService;
    }

    // ==================== 公开接口 ====================

    /**
     * 获取电视剧备案详情（公开）
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<DramaVO> getDramaById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        DramaVO dramaVO = dramaService.getDramaById(id);
        return ApiResponse.success(dramaVO);
    }

    /**
     * 分页获取电视剧备案列表（按时间倒序，公开）
     */
    @GetMapping("/page")
    @PublicAccess
    public PageResponse<DramaVO> getDramaPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Drama> page = new Page<>(current, size);
        Page<DramaVO> dramaPage = dramaService.getDramaPage(page, keyword);
        return PageResponse.of(
                (int) dramaPage.getCurrent(),
                (int) dramaPage.getSize(),
                dramaPage.getTotal(),
                dramaPage.getRecords()
        );
    }

    // ==================== 普通用户接口 ====================

    /**
     * 创建电视剧备案
     */
    @PostMapping
    public ApiResponse<DramaVO> createDrama(@Valid @RequestBody CreateDramaDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        DramaVO dramaVO = dramaService.createDrama(currentUser.getId(), createDTO);
        return ApiResponse.success(dramaVO);
    }

    /**
     * 更新电视剧备案（只能更新自己的）
     */
    @PutMapping("/{id}")
    public ApiResponse<DramaVO> updateDrama(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateDramaDTO updateDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        DramaVO dramaVO = dramaService.updateDrama(currentUser.getId(), id, updateDTO);
        return ApiResponse.success(dramaVO);
    }

    /**
     * 删除电视剧备案（只能删除自己的）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDrama(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        dramaService.deleteDrama(currentUser.getId(), id);
        return ApiResponse.success();
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员创建电视剧备案
     */
    @PostMapping("/admin/create")
    public ApiResponse<DramaVO> createDramaByAdmin(@Valid @RequestBody CreateDramaDTO createDTO) {
        DramaVO dramaVO = dramaService.createDramaByAdmin(createDTO);
        return ApiResponse.success(dramaVO);
    }

    /**
     * 管理员更新电视剧备案
     */
    @PutMapping("/admin/{id}")
    public ApiResponse<DramaVO> updateDramaByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateDramaDTO updateDTO) {
        DramaVO dramaVO = dramaService.updateDramaByAdmin(id, updateDTO);
        return ApiResponse.success(dramaVO);
    }

    /**
     * 管理员删除电视剧备案
     */
    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deleteDramaByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        dramaService.deleteDramaByAdmin(id);
        return ApiResponse.success();
    }

    /**
     * 管理员分页查询电视剧备案
     */
    @GetMapping("/admin/page")
    public PageResponse<DramaVO> getDramaPageByAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Drama> page = new Page<>(current, size);
        Page<DramaVO> dramaPage = dramaService.getDramaPageByAdmin(page, keyword);
        return PageResponse.of(
                (int) dramaPage.getCurrent(),
                (int) dramaPage.getSize(),
                dramaPage.getTotal(),
                dramaPage.getRecords()
        );
    }

    /**
     * 管理员获取电视剧备案详情
     */
    @GetMapping("/admin/{id}")
    public ApiResponse<DramaVO> getDramaByIdByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        DramaVO dramaVO = dramaService.getDramaByIdByAdmin(id);
        return ApiResponse.success(dramaVO);
    }
}
