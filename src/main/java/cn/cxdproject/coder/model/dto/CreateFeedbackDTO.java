package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建反馈DTO
 * 
 * @author heathcetide
 */
@Data
public class CreateFeedbackDTO {
    
    @NotBlank(message = "反馈内容不能为空")
    private String content;
    
    private String type;
}

