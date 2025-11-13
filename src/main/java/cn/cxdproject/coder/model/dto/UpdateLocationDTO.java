package cn.cxdproject.coder.model.dto;

import lombok.Data;

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
    private Byte status;
    private String locationDescription;
    private String contactPhone;
    private String contactName;
    private String address;
    private BigDecimal price;
}

