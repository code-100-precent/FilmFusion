package cn.cxdproject.coder.model.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_policy")
@EqualsAndHashCode(callSuper = true)
public class Policy extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
    * id
    */
    @TableId
    private Long id;

    /**
    * 政策标题
    */
    @TableField("title")
    private String title;

    /**
    * 政策类型："省级" | "市级"
    */
    @TableField("type")
    private String type;

    /**
    * 发布单位
    */
    @TableField("issue_unit")
    private String issueUnit;

    /**
    * 发布时间
    */
    @TableField("issue_time")
    private LocalDateTime issueTime;

    /**
    * 内容
    */
    @TableField("content")
    private String content;

    /**
    * 图片
    */
    @TableField("image")
    private String image;


    /**
    * 压缩图片
    */
    @TableField("thumb_image")
    private String thumbImage;

    @TableField("status")
    private String status;


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
