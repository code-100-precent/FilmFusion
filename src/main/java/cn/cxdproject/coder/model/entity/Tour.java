package cn.cxdproject.coder.model.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;


/**
 * Tour 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_tour")
@EqualsAndHashCode(callSuper = true)
public class Tour extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
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

    /**
    * 主题
    */
    @TableField("theme")
    private String theme;

    /**
    * 特点
    */
    @TableField("features")
    private String features;

    /**
    * 封面
    */
    @TableField("cover")
    private String cover;

    /**
    * 交通方式
    */
    @TableField("transport")
    private String transport;

    /**
    * 周边旅馆
    */
    @TableField("hotel")
    private String hotel;

    /**
    * 美食推荐
    */
    @TableField("food")
    private String food;

    /**
    * images
    */
    @TableField("image")
    private String image;

    @TableField("thumb_cover")
    private String thumbCover;

    @TableField("thumb_image")
    private String thumbImage;

    @TableField("longitude")
    private String longitude;

    @TableField("latitude")
    private String latitude;

    @TableField("location_id")
    private String locationId;

    @Override
    public User clone() {
        try {
            return (User) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone User object", e);
        }
    }

}
