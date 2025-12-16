package cn.cxdproject.coder.model.entity;

import java.io.Serial;
import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Report 实体类
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("fi_reports")
@EqualsAndHashCode(callSuper = true)
public class Report extends BaseEntity implements Serializable, Cloneable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 影视剧名称
     */
    @TableField("name")
    private String name;

    /**
     * 类型
     */
    @TableField("type")
    private String type;

    /**
     * 题材
     */
    @TableField("genre")
    private String genre;

    /**
     * 集数
     */
    @TableField("episodes")
    private Integer episodes;

    /**
     * 投资金额（万元）
     */
    @TableField("invest_amount")
    private BigDecimal investAmount;

    /**
     * 主创人员
     */
    @TableField("main_creators")
    private String mainCreators;

    /**
     * 第一出品单位
     */
    @TableField("lead_producer")
    private String leadProducer;

    /**
     * 制片单位
     */
    @TableField("producer_unit")
    private String producerUnit;

    /**
     * 拍摄开始日期
     */
    @TableField("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 拍摄结束日期
     */
    @TableField("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 剧组规模
     */
    @TableField("crew_scale")
    private String crewScale;

    /**
     * 联系人
     */
    @TableField("contact")
    private String contact;

    /**
     * 联系电话
     */
    @TableField("phone_number")
    private String phoneNumber;

    /**
     * 剧组职务
     */
    @TableField("crew_position")
    private String crewPosition;

    /**
     * 用户ID，关联到用户表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 申报状态（是否处理）
     */
    @TableField("status")
    private String status;

    /**
     * 影视拍摄许可证（可为文档或者图片）
     */
    @TableField(value = "shoot_permit")
    private String shootPermit;

    /**
     * 压缩后影视拍摄许可证（如果是文档则没有压缩后url）
     */
    @TableField(value = "thumb_shoot_permit")
    private String thumbShootPermit;

    /**
     * 立项审批文件（可为文档或者图片）
     */
    @TableField(value = "approval_file")
    private String approvalFile;

    /**
     * 压缩后立项审批文件（如果为文档则没有压缩后url）
     */
    @TableField(value = "thumb_approval_file")
    private String thumbApprovalFile;

    /**
     * 协拍服务申请表（可为文档或者图片）
     */
    @TableField(value = "shoot_apply")
    private String shootApply;

    /**
     * 压缩后协拍服务申请表（如果为文档则没有压缩后url）
     */
    @TableField(value = "thumb_shoot_apply")
    private String thumbShootApply;

    @Override
    public Report clone() {
        try {
            return (Report) super.clone();
        } catch (CloneNotSupportedException e) {
            // This should never happen since we implement Cloneable
            throw new RuntimeException("Failed to clone Report object", e);
        }
    }
}
