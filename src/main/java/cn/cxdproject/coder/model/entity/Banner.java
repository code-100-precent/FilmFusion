package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Banner 实体类
 * @author Hibiscus-code-generate
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_banner")
@EqualsAndHashCode(callSuper = true)
public class Banner  extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    @TableId
    private Long id;

    /**
    * 图片名称
    */
    @TableField("image_name")
    private String imageName;

    /**
    * 图片地址
    */
    @TableField("image_url")
    private String imageUrl;

    /**
    * 跳转模块名称
    */
    @TableField("target_module")
    private String targetModule;


    /**
    * 状态(0:禁用 1:启用)
    */
    @TableField("status")
    private Boolean status;

    /**
    * 排序值，越小越靠前
    */
    @TableField("sort")
    private Long sort;

    @Override
    public Banner clone() {
        try {
            return (Banner) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone User object", e);
        }
    }
}
