package cn.cxdproject.coder.model.vo;

import cn.cxdproject.coder.model.dto.UpdateArticleDTO;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime issueTime;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 用户ID
     */
    private Long userId;

    private String image;

    private String thumbImage;

    /**
     * 封面图片（与image相同，用于前端兼容）
     */
    private String cover;

    /**
     * 缩略封面（与thumbImage相同，用于前端兼容）
     */
    private String thumbCover;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

}

