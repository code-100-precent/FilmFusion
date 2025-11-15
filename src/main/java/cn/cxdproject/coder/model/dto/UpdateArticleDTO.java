package cn.cxdproject.coder.model.dto;

import lombok.Data;

/**
 * 更新文章DTO
 * 
 * @author heathcetide
 */
@Data
public class UpdateArticleDTO {
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 发布单位
     */
    private String issueUnit;
    
    /**
     * 内容
     */
    private String content;

    private String cover;

    private String image;
}

