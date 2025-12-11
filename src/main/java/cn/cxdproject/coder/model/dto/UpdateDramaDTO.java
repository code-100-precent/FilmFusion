package cn.cxdproject.coder.model.dto;

import lombok.Data;

/**
 * 更新电视剧备案DTO
 * 
 * @author heathcetide
 */
@Data
public class UpdateDramaDTO {
    
    private String name;
    private String filingNum;
    private String prodCompany;
    private String crewDescription;
    private String dramaDescription;
    private String cast;
    private String shootLocation;
    private String locationId;
    private String service;
    private String serviceId;
    private String image;
    private String thumbImage;
}

