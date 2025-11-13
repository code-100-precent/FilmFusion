package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateFeedbackDTO;
import cn.cxdproject.coder.model.dto.UpdateFeedbackDTO;
import cn.cxdproject.coder.model.entity.Feedback;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.FeedbackVO;
import cn.cxdproject.coder.service.FeedbackService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 反馈控制器
 * 
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/feedback")
@Validated
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    // ==================== 普通用户接口 ====================

    /**
     * 创建反馈
     */
    @PostMapping
    public ApiResponse<FeedbackVO> createFeedback(@Valid @RequestBody CreateFeedbackDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        FeedbackVO feedbackVO = feedbackService.createFeedback(currentUser.getId(), createDTO);
        return ApiResponse.success(feedbackVO);
    }

    /**
     * 获取自己的反馈列表
     */
    @GetMapping("/my")
    public PageResponse<FeedbackVO> getMyFeedbackPage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            PageResponse<FeedbackVO> errorResponse = new PageResponse<>();
            errorResponse.setCode(401);
            errorResponse.setMessage("未登录");
            return errorResponse;
        }
        Page<Feedback> page = new Page<>(current, size);
        Page<FeedbackVO> feedbackPage = feedbackService.getMyFeedbackPage(currentUser.getId(), page);
        return PageResponse.of(
                (int) feedbackPage.getCurrent(),
                (int) feedbackPage.getSize(),
                feedbackPage.getTotal(),
                feedbackPage.getRecords()
        );
    }

    /**
     * 获取自己的反馈详情
     */
    @GetMapping("/my/{id}")
    public ApiResponse<FeedbackVO> getMyFeedbackById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        FeedbackVO feedbackVO = feedbackService.getMyFeedbackById(currentUser.getId(), id);
        return ApiResponse.success(feedbackVO);
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员更新反馈状态
     */
    @PutMapping("/admin/{id}")
    public ApiResponse<FeedbackVO> updateFeedbackByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateFeedbackDTO updateDTO) {
        FeedbackVO feedbackVO = feedbackService.updateFeedbackByAdmin(id, updateDTO);
        return ApiResponse.success(feedbackVO);
    }

    /**
     * 管理员删除反馈
     */
    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> deleteFeedbackByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        feedbackService.deleteFeedbackByAdmin(id);
        return ApiResponse.success();
    }

    /**
     * 管理员分页查询反馈
     */
    @GetMapping("/admin/page")
    public PageResponse<FeedbackVO> getFeedbackPageByAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        Page<Feedback> page = new Page<>(current, size);
        Page<FeedbackVO> feedbackPage = feedbackService.getFeedbackPageByAdmin(page, keyword, status);
        return PageResponse.of(
                (int) feedbackPage.getCurrent(),
                (int) feedbackPage.getSize(),
                feedbackPage.getTotal(),
                feedbackPage.getRecords()
        );
    }

    /**
     * 管理员获取反馈详情
     */
    @GetMapping("/admin/{id}")
    public ApiResponse<FeedbackVO> getFeedbackByIdByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        FeedbackVO feedbackVO = feedbackService.getFeedbackByIdByAdmin(id);
        return ApiResponse.success(feedbackVO);
    }
}
