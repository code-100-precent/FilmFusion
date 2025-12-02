package cn.cxdproject.coder.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_hotel")
@EqualsAndHashCode(callSuper = true)
public class Hotel extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 住宿名称
     */
    @TableField("name")
    private String name;

    /**
     * 介绍
     */
    @TableField("description")
    private String description;

    /**
     * 地址
     */
    @TableField("address")
    private String address;


    /**
     * 负责人名称
     */
    @TableField("manager_name")
    private String managerName;

    /**
     * 负责人电话
     */
    @TableField("manager_phone")
    private String managerPhone;

    /**
     * 图片
     */
    @TableField("image")
    private String image;

    @TableField("user_id")
    private Long userId;

    @TableField("thumb_image")
    private String thumbImage;

    @TableField("longitude")
    private String longitude;

    @TableField("latitude")
    private String latitude;

    @Override
    public Hotel clone() {
        try {
            return (Hotel) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone FiAccommodation object", e);
        }
    }

}
