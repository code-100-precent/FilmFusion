package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Tour 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_tours")
@EqualsAndHashCode(callSuper = true)
public class Tour extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 体验游名称
     */
    @TableField("name")
    private String name;

    /**
     * 介绍
     */
    @TableField("description")
    private String description;

    @Override
    public Tour clone() {
        try {
            return (Tour) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone Tour object", e);
        }
    }
}
