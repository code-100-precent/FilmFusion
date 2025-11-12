package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Feedback 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_feedbacks")
@EqualsAndHashCode(callSuper = true)
public class Feedback extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
    * 主键ID
    */
    @TableId
    private Long id;

    /**
    * 用户ID，关联到用户表
    */
    @TableField("user_id")
    private Long userId;

    /**
    * 反馈内容
    */
    @TableField("content")
    private String content;

    /**
    * 反馈状态（未处理、处理中、已解决等）
    */
    @TableField("status")
    private String status;

    /**
    * 反馈类型（建议、投诉等）
    */
    @TableField("type")
    private String type;

    @Override
    public Feedback clone() {
        try {
            return (Feedback) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone User object", e);
        }
    }
}
