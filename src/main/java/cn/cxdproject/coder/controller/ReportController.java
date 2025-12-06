package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateReportDTO;
import cn.cxdproject.coder.model.dto.UpdateReportAdminDTO;
import cn.cxdproject.coder.model.dto.UpdateReportUserDTO;
import cn.cxdproject.coder.model.entity.Report;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ReportVO;
import cn.cxdproject.coder.service.ReportService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 影视剧备案控制器
 * 
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/report")
@Validated
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 获取影视剧备案详情（公开）
     */
    @GetMapping("/{id}")
    public ApiResponse<ReportVO> getReportById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        ReportVO reportVO = reportService.getReportById(id,currentUser.getId());
        return ApiResponse.success(reportVO);
    }

    // ==================== 普通用户接口 ====================

    /**
     * 创建影视剧备案
     */
    @PostMapping("/create")
    public ApiResponse<ReportVO> createReport(@Valid @RequestBody CreateReportDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        ReportVO reportVO = reportService.createReport(currentUser.getId(), createDTO);
        return ApiResponse.success(reportVO);
    }

    /**
     * 更新影视剧备案（只能更新自己的）
     */
    @PutMapping("/update/{id}")
    public ApiResponse<ReportVO> updateReport(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateReportUserDTO updateDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        ReportVO reportVO = reportService.updateReport(currentUser.getId(), id, updateDTO);
        return ApiResponse.success(reportVO);
    }

    /**
     * 删除影视剧备案（只能删除自己的）
     */
    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteReport(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        reportService.deleteReport(currentUser.getId(), id);
        return ApiResponse.success();
    }

    /**
     * 获取我的报备列表
     */
    @GetMapping("/my")
    public PageResponse<ReportVO> getMyReportPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        // 拦截器已经验证了登录状态，这里直接获取用户信息
        User currentUser = AuthContext.getCurrentUser();
        log.debug("getMyReportPage - currentUser: {}", currentUser != null ? "userId=" + currentUser.getId() : "null");
        
        if (currentUser == null) {
            // 防御性检查：如果拦截器没有正确设置用户信息，返回401
            log.error("getMyReportPage - currentUser为null，拦截器可能未正确执行");
            PageResponse<ReportVO> errorResponse = new PageResponse<>();
            errorResponse.setCode(401);
            errorResponse.setMessage("未登录或token已过期");
            return errorResponse;
        }
        Page<Report> page = new Page<>(current, size);
        Page<ReportVO> reportPage = reportService.getMyReportPage(currentUser.getId(), page);
        return PageResponse.of(
                (int) reportPage.getCurrent(),
                (int) reportPage.getSize(),
                reportPage.getTotal(),
                reportPage.getRecords()
        );
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员更新影视剧备案
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<ReportVO> updateReportByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateReportAdminDTO updateDTO) {
        ReportVO reportVO = reportService.updateReportByAdmin(id, updateDTO);
        return ApiResponse.success(reportVO);
    }
    /**
     * 管理员分页查询影视剧备案
     */
    @GetMapping("/admin/page")
    public PageResponse<ReportVO> getReportPageByAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Report> page = new Page<>(current, size);
        Page<ReportVO> reportPage = reportService.getReportPageByAdmin(page, keyword);
        return PageResponse.of(
                (int) reportPage.getCurrent(),
                (int) reportPage.getSize(),
                reportPage.getTotal(),
                reportPage.getRecords()
        );
    }

    /**
     * 管理员获取影视剧备案详情
     */
    @GetMapping("/admin/{id}")
    public ApiResponse<ReportVO> getReportByIdByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        ReportVO reportVO = reportService.getReportByIdByAdmin(id);
        return ApiResponse.success(reportVO);
    }
}
