package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建文章DTO
 * 
 * @author heathcetide
 */
@Data
public class CreateArticleDTO {
    
    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    /**
     * 发布单位
     */
    @NotBlank(message = "发布单位不能为空")
    private String issueUnit;
    
    /**
     * 内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String cover;

    private String image;
}

