package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * Drama 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_dramas")
@EqualsAndHashCode(callSuper = true)
public class Drama extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 电视剧名称
     */
    @TableField("name")
    private String name;

    /**
     * 备案号
     */
    @TableField("filing_num")
    private String filingNum;

    /**
     * 出品公司
     */
    @TableField("prod_company")
    private String prodCompany;

    /**
     * 公司简介
     */
    @TableField("crew_description")
    private String crewDescription;

    /**
     * 电视剧简介
     */
    @TableField("drama_description")
    private String dramaDescription;

    /**
     * 演员名单
     */
    @TableField("cast")
    private String cast;

    /**
     * 拍摄地
     */
    @TableField("shoot_location")
    private String shootLocation;

    /**
     * 拍摄地ID，关联locations表
     */
    @TableField("location_id")
    private Long locationId;

    /**
     * 协拍服务
     */
    @TableField("service")
    private String service;

    /**
     * 协拍服务ID，关联shoot表
     */
    @TableField("service_id")
    private Long serviceId;

    /**
     * 用户ID，关联到用户表
     */
    @TableField("user_id")
    private Long userId;

    @Override
    public Drama clone() {
        try {
            return (Drama) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone Drama object", e);
        }
    }
}
