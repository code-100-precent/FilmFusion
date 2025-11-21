package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.service.LocationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 拍摄场地控制器
 *
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/location")
@Validated
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    // ==================== 公开接口 ====================

    /**
     * 获取拍摄场地详情（公开）
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<LocationVO> getLocationById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        LocationVO locationVO = locationService.getLocationById(id);
        return ApiResponse.success(locationVO);
    }

    /**
     * 分页获取拍摄场地列表（按时间倒序，公开，只显示可用的）
     */
    @GetMapping("/page")
    @PublicAccess
    public PageResponse<LocationVO> getLocationPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Location> page = new Page<>(current, size);
        Page<LocationVO> locationPage = locationService.getLocationPage(page, keyword);
        return PageResponse.of(
                (int) locationPage.getCurrent(),
                (int) locationPage.getSize(),
                locationPage.getTotal(),
                locationPage.getRecords()
        );
    }

    // ==================== 普通用户接口 ====================

    /**
     * 创建拍摄场地
     */
    @PostMapping
    public ApiResponse<LocationVO> createLocation(@Valid @RequestBody CreateLocationDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        LocationVO locationVO = locationService.createLocation(currentUser.getId(), createDTO);
        return ApiResponse.success(locationVO);
    }

    /**
     * 更新拍摄场地（只能更新自己的）
     */
    @PutMapping("/update/{id}")
    public ApiResponse<LocationVO> updateLocation(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateLocationDTO updateDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        LocationVO locationVO = locationService.updateLocation(currentUser.getId(), id, updateDTO);
        return ApiResponse.success(locationVO);
    }

    /**
     * 删除拍摄场地（只能删除自己的）
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteLocation(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        locationService.deleteLocation(currentUser.getId(), id);
        return ApiResponse.success();
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员更新拍摄场地
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<LocationVO> updateLocationByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateLocationDTO updateDTO) {
        LocationVO locationVO = locationService.updateLocationByAdmin(id, updateDTO);
        return ApiResponse.success(locationVO);
    }

    /**
     * 管理员删除拍摄场地
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteLocationByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        locationService.deleteLocationByAdmin(id);
        return ApiResponse.success();
    }

}
