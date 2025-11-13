package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateFeedbackDTO;
import cn.cxdproject.coder.model.dto.UpdateFeedbackDTO;
import cn.cxdproject.coder.model.entity.Feedback;
import cn.cxdproject.coder.model.vo.FeedbackVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Feedback 服务接口
 * @author Hibiscus-code-generate
 */
public interface FeedbackService extends IService<Feedback> {
    
    /**
     * 创建反馈（普通用户）
     */
    FeedbackVO createFeedback(Long userId, CreateFeedbackDTO createDTO);
    
    /**
     * 获取自己的反馈列表
     */
    Page<FeedbackVO> getMyFeedbackPage(Long userId, Page<Feedback> page);
    
    /**
     * 获取自己的反馈详情
     */
    FeedbackVO getMyFeedbackById(Long userId, Long feedbackId);
    
    /**
     * 管理员更新反馈状态
     */
    FeedbackVO updateFeedbackByAdmin(Long feedbackId, UpdateFeedbackDTO updateDTO);
    
    /**
     * 管理员删除反馈
     */
    void deleteFeedbackByAdmin(Long feedbackId);
    
    /**
     * 管理员分页查询反馈
     */
    Page<FeedbackVO> getFeedbackPageByAdmin(Page<Feedback> page, String keyword, String status);
    
    /**
     * 管理员获取反馈详情
     */
    FeedbackVO getFeedbackByIdByAdmin(Long feedbackId);
    
    /**
     * 转换为VO
     */
    FeedbackVO toFeedbackVO(Feedback feedback);
}
