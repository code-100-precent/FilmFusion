package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateFeedbackDTO;
import cn.cxdproject.coder.model.dto.UpdateFeedbackDTO;
import cn.cxdproject.coder.model.entity.Feedback;
import cn.cxdproject.coder.model.vo.FeedbackVO;
import cn.cxdproject.coder.mapper.FeedbackMapper;
import cn.cxdproject.coder.service.FeedbackService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Feedback 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedbackVO createFeedback(Long userId, CreateFeedbackDTO createDTO) {
        Feedback feedback = Feedback.builder()
                .userId(userId)
                .content(createDTO.getContent())
                .type(createDTO.getType())
                .status("未处理")
                .build();

        this.save(feedback);
        return toFeedbackVO(feedback);
    }

    @Override
    public Page<FeedbackVO> getMyFeedbackPage(Long userId, Page<Feedback> page) {
        QueryWrapper<Feedback> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);
        wrapper.eq("user_id", userId);

        wrapper.orderByDesc("created_at");

        Page<Feedback> feedbackPage = this.page(page, wrapper);
        Page<FeedbackVO> voPage = new Page<>(feedbackPage.getCurrent(), feedbackPage.getSize(), feedbackPage.getTotal());
        List<FeedbackVO> voList = feedbackPage.getRecords().stream()
                .map(this::toFeedbackVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public FeedbackVO getMyFeedbackById(Long userId, Long feedbackId) {
        Feedback feedback = this.getById(feedbackId);
        if (feedback == null || Boolean.TRUE.equals(feedback.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "反馈不存在");
        }

        if (!feedback.getUserId().equals(userId)) {
            throw new NotFoundException(NOT_FOUND.code(), "反馈不存在");
        }

        return toFeedbackVO(feedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FeedbackVO updateFeedbackByAdmin(Long feedbackId, UpdateFeedbackDTO updateDTO) {
        Feedback feedback = this.getById(feedbackId);
        if (feedback == null || Boolean.TRUE.equals(feedback.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "反馈不存在");
        }

        if (updateDTO.getStatus() != null) {
            feedback.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getType() != null) {
            feedback.setType(updateDTO.getType());
        }

        this.updateById(feedback);
        return toFeedbackVO(feedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteFeedbackByAdmin(Long feedbackId) {
        Feedback feedback = this.getById(feedbackId);
        if (feedback == null || Boolean.TRUE.equals(feedback.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "反馈不存在");
        }

        feedback.setDeleted(true);
        this.updateById(feedback);
    }

    @Override
    public Page<FeedbackVO> getFeedbackPageByAdmin(Page<Feedback> page, String keyword, String status) {
        QueryWrapper<Feedback> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("content", keyword));
        }

        if (status != null && !status.isEmpty()) {
            wrapper.eq("status", status);
        }

        wrapper.orderByDesc("created_at");

        Page<Feedback> feedbackPage = this.page(page, wrapper);
        Page<FeedbackVO> voPage = new Page<>(feedbackPage.getCurrent(), feedbackPage.getSize(), feedbackPage.getTotal());
        List<FeedbackVO> voList = feedbackPage.getRecords().stream()
                .map(this::toFeedbackVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public FeedbackVO getFeedbackByIdByAdmin(Long feedbackId) {
        Feedback feedback = this.getById(feedbackId);
        if (feedback == null || Boolean.TRUE.equals(feedback.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "反馈不存在");
        }
        return toFeedbackVO(feedback);
    }

    @Override
    public FeedbackVO toFeedbackVO(Feedback feedback) {
        if (feedback == null) {
            return null;
        }
        FeedbackVO vo = new FeedbackVO();
        BeanUtils.copyProperties(feedback, vo);
        return vo;
    }
}
