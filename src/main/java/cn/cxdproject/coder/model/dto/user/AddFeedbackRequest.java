package cn.cxdproject.coder.model.dto.user;

import lombok.Data;

@Data
public class AddFeedbackRequest {

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 反馈类型（建议、投诉等）
     */
    private String type;
}
