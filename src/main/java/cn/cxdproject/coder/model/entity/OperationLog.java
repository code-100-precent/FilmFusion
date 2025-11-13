package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * OperationLog 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_operation_logs")
@EqualsAndHashCode(callSuper = true)
public class OperationLog extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 操作类型（如：LOGIN、DOC_EDIT）
     */
    @TableField("type")
    private String type;

    /**
     * 操作描述
     */
    @TableField("description")
    private String description;

    /**
     * 执行用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作人标识（用户名或IP）
     */
    @TableField("operator")
    private String operator;

    /**
     * 是否成功
     */
    @TableField("success")
    private Boolean success;

    /**
     * 输入参数快照
     */
    @TableField("params")
    private String params;

    /**
     * 输出结果快照
     */
    @TableField("result")
    private String result;

    /**
     * 操作时间
     */
    @TableField("timestamp")
    private LocalDateTime timestamp;

    @Override
    public OperationLog clone() {
        try {
            return (OperationLog) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone OperationLog object", e);
        }
    }
}
