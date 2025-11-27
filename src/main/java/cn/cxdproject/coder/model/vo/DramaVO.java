package cn.cxdproject.coder.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 电视剧备案视图对象
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DramaVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 电视剧名称
     */
    private String name;
    
    /**
     * 备案号
     */
    private String filingNum;
    
    /**
     * 出品公司
     */
    private String prodCompany;
    
    /**
     * 公司简介
     */
    private String crewDescription;
    
    /**
     * 电视剧简介
     */
    private String dramaDescription;
    
    /**
     * 演员名单
     */
    private String cast;
    
    /**
     * 拍摄地
     */
    private String shootLocation;
    
    /**
     * 拍摄地ID，关联locations表
     */
    private Long locationId;
    
    /**
     * 协拍服务
     */
    private String service;
    
    /**
     * 协拍服务ID，关联shoot表
     */
    private Long serviceId;
    
    /**
     * 用户ID
     */
    private Long userId;

    private String cover;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String image;

    private String thumbCover;

    private String thumbImage;


}

