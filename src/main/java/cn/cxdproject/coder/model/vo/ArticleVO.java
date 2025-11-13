package cn.cxdproject.coder.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章视图对象
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleVO {
    
    /**
     * 文章ID
     */
    private Long id;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 发布单位
     */
    private String issueUnit;
    
    /**
     * 发布时间
     */
    private LocalDateTime issueTime;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

