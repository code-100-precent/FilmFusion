package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 创建影视剧备案DTO
 * 
 * @author heathcetide
 */
@Data
public class CreateReportDTO {
    
    @NotBlank(message = "影视剧名称不能为空")
    private String name;
    
    @NotBlank(message = "类型不能为空")
    private String type;
    
    @NotBlank(message = "题材不能为空")
    private String genre;
    
    @NotNull(message = "集数不能为空")
    private Integer episodes;
    
    @NotNull(message = "投资金额不能为空")
    private BigDecimal investAmount;
    
    @NotBlank(message = "主创人员不能为空")
    private String mainCreators;
    
    @NotBlank(message = "第一出品单位不能为空")
    private String leadProducer;
    
    @NotBlank(message = "制片单位不能为空")
    private String producerUnit;
    
    @NotNull(message = "拍摄开始日期不能为空")
    private LocalDate startDate;
    
    @NotNull(message = "拍摄结束日期不能为空")
    private LocalDate endDate;
    
    @NotBlank(message = "剧组规模不能为空")
    private String crewScale;
    
    @NotBlank(message = "联系人不能为空")
    private String contact;
    
    @NotBlank(message = "联系电话不能为空")
    private String phoneNumber;
    
    @NotBlank(message = "剧组职务不能为空")
    private String crewPosition;

    @NotBlank(message = "申请状态")
    private String status;

    private String image;


}

