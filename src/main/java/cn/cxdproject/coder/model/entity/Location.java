package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Location 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_locations")
@EqualsAndHashCode(callSuper = true)
public class Location extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 场地名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 可用状态（0：不可用，1：可用）
     */
    @TableField("status")
    private Boolean status;

    /**
     * 场地介绍
     */
    @TableField("location_description")
    private String locationDescription;

    /**
     * 联系人电话
     */
    @TableField("contact_phone")
    private String contactPhone;

    /**
     * 联系人
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 地址
     */
    @TableField("address")
    private String address;

    /**
     * 价格（元）
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 用户ID，关联到用户表
     */
    @TableField("user_id")
    private Long userId;

    @TableField("cover")
    private String cover;

    @TableField("image")
    private String image;

    @TableField("deleted")
    private Boolean deleted;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;

    @Override
    public Location clone() {
        try {
            return (Location) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone Location object", e);
        }
    }
}
