package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.CreateTourDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.entity.Tour;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.model.vo.TourVO;
import cn.cxdproject.coder.service.TourService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Tour 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/tour")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    /**
     * 新增 Tour 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<TourVO> getTourById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        TourVO tourVO = tourService.getTourById(id);
        return ApiResponse.success(tourVO);
    }

    /**
     * 分页获取路线服务列表（按时间倒序，公开，只显示上线的）
     */
    @GetMapping("/page")
    @PublicAccess
    public PageResponse<TourVO> getTourPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Tour> page = new Page<>(current, size);
        Page<TourVO> tourPage = tourService.getTourPage(page, keyword);
        return PageResponse.of(
                (int) tourPage.getCurrent(),
                (int) tourPage.getSize(),
                tourPage.getTotal(),
                tourPage.getRecords()
        );
    }

    // ==================== 管理员接口 ====================

    /**
     * 创建路线服务
     */
    @PostMapping("/admin/create")
    public ApiResponse<TourVO> createTour(@Valid @RequestBody CreateTourDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        TourVO tourVO = tourService.createTourByAdmin(createDTO);
        return ApiResponse.success(tourVO);
    }


    /**
     * 管理员更新路线服务
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<TourVO> updateTourByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateTourDTO updateDTO) {
        TourVO tourVO = tourService.updateTourByAdmin(id, updateDTO);
        return ApiResponse.success(tourVO);
    }

    /**
     * 管理员删除路线服务
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteTourByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        tourService.deleteTourByAdmin(id);
        return ApiResponse.success();
    }

}
