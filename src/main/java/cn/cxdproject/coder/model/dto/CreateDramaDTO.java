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
    
    @NotBlank(message = "公司简介不能为空")
    private String crewDescription;
    
    @NotBlank(message = "电视剧简介不能为空")
    private String dramaDescription;
    
    @NotBlank(message = "演员名单不能为空")
    private String cast;
    
    @NotBlank(message = "拍摄地不能为空")
    private String shootLocation;
    
    @NotNull(message = "拍摄地ID不能为空")
    private String locationId;
    
    @NotBlank(message = "协拍服务不能为空")
    private String service;
    
    @NotNull(message = "协拍服务ID不能为空")
    private String serviceId;

    private String image;

    private String thumbImage;
}

