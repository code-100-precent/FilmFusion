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
 * Shoot 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_shoots")
@EqualsAndHashCode(callSuper = true)
public class Shoot extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 服务名称
     */
    @TableField("name")
    private String name;

    /**
     * 服务简介
     */
    @TableField("description")
    private String description;

    /**
     * 价格（元）
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 状态（0：下线，1：上线）
     */
    @TableField("status")
    private Boolean status;

    /**
     * 服务地址
     */
    @TableField("address")
    private String address;

    /**
     * 联系电话
     */
    @TableField("phone")
    private String phone;

    /**
     * 联系人
     */
    @TableField("contact_name")
    private String contactName;

    /**
     * 用户ID，关联到用户表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 图片url(第一张为封面)
     */
    @TableField("image")
    private String image;

    /**
     * 压缩后图片url(第一张为封面)
     */
    @TableField("thumb_image")
    private String thumbImage;

    @Override
    public Shoot clone() {
        try {
            return (Shoot) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone Shoot object", e);
        }
    }
}
