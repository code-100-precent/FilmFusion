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
     * 场地联系人电话
     */
    @TableField("location_principal_phone")
    private String locationPrincipalPhone;

    /**
     * 场地联系人
     */
    @TableField("location_principal_name")
    private String locationPrincipalName;

    /**
     * 政府联系人电话
     */
    @TableField("gov_principal_phone")
    private String govPrincipalPhone;

    /**
     * 政府联系人姓名
     */
    @TableField("gov_principal_name")
    private String govPrincipalName;

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

    /**
     * 经度
     */
    @TableField("longitude")
    private String longitude;

    /**
     * 纬度
     */
    @TableField("latitude")
    private String latitude;

    //关联影视剧
    @TableField("drama_id")
    private String dramaId;

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
