package cn.cxdproject.coder.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
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
    private String contactPhone;
    private String contactName;
    private String address;
    private BigDecimal price;
    private Long userId;
    private String cover;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    public LocationVO(Long id, String name, Boolean status, String type, String locationDescription, String cover, String address, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.status = status;
        this.locationDescription = locationDescription;
        this.address = address;
        this.price = price;
        this.cover = cover;
    }
}

