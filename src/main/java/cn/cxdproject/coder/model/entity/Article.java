package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Article 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_articles")
@EqualsAndHashCode(callSuper = true)
public class Article extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
    * 主键ID
    */
    @TableId
    private Long id;

    /**
    * 文章标题
    */
    @TableField("title")
    private String title;

    /**
    * 发布单位
    */
    @TableField("issue_unit")
    private String issueUnit;

    /**
    * 发布时间
    */
    @TableField("issue_time")
    private LocalDateTime issueTime;

    /**
    * 内容
    */
    @TableField("content")
    private String content;

    /**
    * 用户ID，关联到用户表
    */
    @TableField("user_id")
    private Long userId;

    @Override
    public Article clone() {
        try {
            return (Article) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone User object", e);
        }
    }
}
