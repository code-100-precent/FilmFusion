package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Module 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_module")
@EqualsAndHashCode(callSuper = true)
public class Module extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 模块名称
     */
    @TableField("name")
    private String name;

    /**
     * 模块描述
     */
    @TableField("description")
    private String description;

    @Override
    public Module clone() {
        try {
            return (Module) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone Module object", e);
        }
    }
}
