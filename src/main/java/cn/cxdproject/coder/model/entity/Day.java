package cn.cxdproject.coder.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Day 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_days")
@EqualsAndHashCode(callSuper = true)
public class Day extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 关联tour_id
     */
    @TableField("tour_id")
    private Long tourId;

    /**
     * 旅游每日专题名称
     */
    @TableField("name")
    private String name;

    /**
     * 第几天
     */
    @TableField("day")
    private String day;

    @Override
    public Day clone() {
        try {
            return (Day) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone Day object", e);
        }
    }
}
