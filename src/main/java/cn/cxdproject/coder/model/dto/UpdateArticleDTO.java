package cn.cxdproject.coder.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime issueTime;

    /**
     * 发布单位
     */
    private String issueUnit;
    
    /**
     * 内容
     */
    private String content;

    private String image;

    private String thumbImage;
}

