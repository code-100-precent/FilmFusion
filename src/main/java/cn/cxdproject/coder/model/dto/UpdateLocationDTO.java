package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

/**
 * 更新拍摄场地DTO
 * 
 * @author heathcetide
 */
@Data
public class UpdateLocationDTO {
    
    private String name;
    private String type;
    private Boolean status;
    private String locationDescription;
    private String locationPrincipalPhone;
    private String locationPrincipalName;
    private String govPrincipalPhone;
    private String govPrincipalName;
    private String address;
    private BigDecimal price;
    private String cover;
    private String image;
    private String thumbCover;
    private String thumbImage;
    private String longitude;
    private String latitude;
}

