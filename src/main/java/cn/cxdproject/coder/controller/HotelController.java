package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateHotelDTO;
import cn.cxdproject.coder.model.dto.UpdateHotelDTO;
import cn.cxdproject.coder.model.entity.Hotel;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.HotelVO;
import cn.cxdproject.coder.service.impl.HotelServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/api/hotel")
@Validated
public class HotelController {

    private final HotelServiceImpl hotelService;

    public HotelController(HotelServiceImpl hotelService) {
        this.hotelService = hotelService;
    }


    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<HotelVO> getHotelById(@PathVariable @NotNull(message = "旅店ID不能为空") Long id) {

        HotelVO hotelVO = hotelService.getHotelById(id);
        return ApiResponse.success(hotelVO);
    }

    /**
     * 分页获取旅店列表（按时间倒序，公开）
     */
    @GetMapping("/page")
    @PublicAccess
    public PageResponse<HotelVO> getHotelPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Hotel> page = new Page<>(current, size);
        Page<HotelVO> hotelPage = hotelService.getHotelPage(page, keyword);
        return PageResponse.of(
                (int) hotelPage.getCurrent(),
                (int) hotelPage.getSize(),
                hotelPage.getTotal(),
                hotelPage.getRecords()
        );
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员创建旅店信息
     */
    @PostMapping("/admin/create")
    public ApiResponse<HotelVO> createHotelByAdmin(@Valid @RequestBody CreateHotelDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        HotelVO hotelVO = hotelService.createHotelByAdmin(currentUser.getId(),createDTO);
        return ApiResponse.success(hotelVO);
    }

    /**
     * 管理员更新旅店
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<HotelVO> updateHotelByAdmin(
            @PathVariable @NotNull(message = "文章ID不能为空") Long id,
            @Valid @RequestBody UpdateHotelDTO updateDTO) {
        // 权限检查在拦截器中完成
        HotelVO hotelVO = hotelService.updateHotelByAdmin(id, updateDTO);
        return ApiResponse.success(hotelVO);
    }

    /**
     * 管理员删除旅店
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteHotelByAdmin(@PathVariable @NotNull(message = "旅店ID不能为空") Long id) {
        // 权限检查在拦截器中完成
        hotelService.deleteHotelByAdmin(id);
        return ApiResponse.success();
    }
}
