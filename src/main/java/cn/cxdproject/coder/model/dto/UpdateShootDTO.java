package cn.cxdproject.coder.model.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新协拍服务DTO
 * 
 * @author heathcetide
 */
@Data
public class UpdateShootDTO {
    
    private String name;
    private String description;
    private Boolean status;
    private String address;
    private String phone;
    private String contactName;
    private String image;
    private String thumbImage;
}

