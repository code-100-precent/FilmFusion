package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建电视剧备案DTO
 * 
 * @author heathcetide
 */
@Data
public class CreateDramaDTO {
    
    @NotBlank(message = "电视剧名称不能为空")
    private String name;
    
    @NotBlank(message = "备案号不能为空")
    private String filingNum;
    
    @NotBlank(message = "出品公司不能为空")
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

