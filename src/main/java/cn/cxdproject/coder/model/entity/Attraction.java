package cn.cxdproject.coder.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * Attraction 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_attractions")
@EqualsAndHashCode(callSuper = true)
public class Attraction extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 关联day_id
     */
    @TableField("day_id")
    private Long dayId;

    /**
     * 关联tour_id
     */
    @TableField("tour_id")
    private Long tourId;

    /**
     * 名称
     */
    @TableField("name")
    private String name;

    /**
     * 亮点
     */
    @TableField("highlights")
    private String highlights;

    /**
     * 附近景点
     */
    @TableField("location_id")
    private String locationId;

    /**
     * 相关影视作品
     */
    @TableField("drama_id")
    private String dramaId;

    /**
     * 附近酒店
     */
    @TableField("hotel_id")
    private String hotelId;

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
    public Attraction clone() {
        try {
            return (Attraction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone Attraction object", e);
        }
    }
}
