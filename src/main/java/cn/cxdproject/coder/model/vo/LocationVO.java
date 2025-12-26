package cn.cxdproject.coder.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 拍摄场地视图对象
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationVO {
    
    private Long id;
    private String name;
    private String type;
    private Boolean status;
    private String locationDescription;
    private String locationPrincipalPhone;
    private String locationPrincipalName;
    private String govPrincipalPhone;
    private String govPrincipalName;
    private String address;
    private Long userId;
    private String image;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    private String thumbImage;
    private String longitude;
    private String latitude;
    private String dramaId;

}

