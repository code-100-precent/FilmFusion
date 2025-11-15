package cn.cxdproject.coder.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 协拍服务视图对象
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShootVO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean status;
    private String address;
    private String phone;
    private String contactName;
    private Long userId;
    private String cover;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShootVO(Long id, String name, String description, BigDecimal price, Boolean status, String cover, String address) {
        this.id=id;
        this.name=name;
        this.description=description;
        this.price=price;
        this.status=status;
        this.cover=cover;
        this.address=address;
    }
}

