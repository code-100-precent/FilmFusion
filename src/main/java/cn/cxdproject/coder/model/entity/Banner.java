package cn.cxdproject.coder.model.entity;

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
    * 是否删除(0:未删 1:已删)
    */
    @TableField("deleted")
    private Boolean deleted;

    /**
    * 创建时间
    */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
    * 更新时间
    */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
    * 排序值，越小越靠前
    */
    @TableField("sort")
    private Long sort;


    public Long getId() {
    return id;
    }

    public void setId(Long id) {
    this.id = id;
    }
    public String getImageName() {
    return imageName;
    }

    public void setImageName(String imageName) {
    this.imageName = imageName;
    }
    public String getImageUrl() {
    return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    }
    public String getTargetModule() {
    return targetModule;
    }

    public void setTargetModule(String targetModule) {
    this.targetModule = targetModule;
    }

    public Boolean getStatus() {
    return status;
    }

    public void setStatus(Boolean status) {
    this.status = status;
    }
    public Boolean getDeleted() {
    return deleted;
    }

    public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
    }
    public LocalDateTime getCreatedAt() {
    return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
    }
    public LocalDateTime getUpdatedAt() {
    return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
    }
    public Long getSort() {
    return sort;
    }

    public void setSort(Long sort) {
    this.sort = sort;
    }
}
