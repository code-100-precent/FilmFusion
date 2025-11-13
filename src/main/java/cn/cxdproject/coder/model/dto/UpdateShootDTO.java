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
    private BigDecimal price;
    private Byte status;
    private String address;
    private String phone;
    private String contactName;
}

