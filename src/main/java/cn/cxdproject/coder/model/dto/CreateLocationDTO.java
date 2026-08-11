package cn.cxdproject.coder.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
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

    private String locationPrincipalPhone;

    private String locationPrincipalName;

    private String govPrincipalPhone;

    private String govPrincipalName;
    
    @NotBlank(message = "地址不能为空")
    private String address;

    private String image;

    private String thumbImage;

    private String longitude;

    private String latitude;

    private String dramaId;
}

