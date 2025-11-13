package cn.cxdproject.coder.model.vo;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户视图对象（脱敏）
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 电话号码（原始值，不直接暴露给JSON）
     */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String phoneNumber;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 角色
     */
    private String role;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 获取脱敏后的电话号码（用于JSON序列化）
     */
    @JsonGetter("phoneNumber")
    public String getMaskedPhoneNumber() {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }
        if (phoneNumber.length() <= 7) {
            return phoneNumber;
        }
        // 保留前3位和后4位，中间用*代替
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}

