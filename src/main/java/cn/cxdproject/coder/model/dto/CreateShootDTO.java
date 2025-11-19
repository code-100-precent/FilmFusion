package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建协拍服务DTO
 * 
 * @author heathcetide
 */
@Data
public class CreateShootDTO {
    
    @NotBlank(message = "服务名称不能为空")
    private String name;
    
    @NotBlank(message = "服务简介不能为空")
    private String description;
    
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    
    @NotNull(message = "状态不能为空")
    private Boolean status;
    
    @NotBlank(message = "服务地址不能为空")
    private String address;
    
    @NotBlank(message = "联系电话不能为空")
    private String phone;
    
    @NotBlank(message = "联系人不能为空")
    private String contactName;

    private String cover;

    private String image;
}

