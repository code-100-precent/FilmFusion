package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建拍摄场地DTO
 * 
 * @author heathcetide
 */
@Data
public class CreateLocationDTO {
    
    @NotBlank(message = "场地名称不能为空")
    private String name;
    
    @NotBlank(message = "类型不能为空")
    private String type;
    
    @NotNull(message = "可用状态不能为空")
    private Boolean status;
    
    @NotBlank(message = "场地介绍不能为空")
    private String locationDescription;
    
    @NotBlank(message = "联系人电话不能为空")
    private String contactPhone;
    
    @NotBlank(message = "联系人不能为空")
    private String contactName;
    
    @NotBlank(message = "地址不能为空")
    private String address;
    
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private String cover;

    private String image;
}

