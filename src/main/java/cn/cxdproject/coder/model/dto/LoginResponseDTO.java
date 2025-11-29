package cn.cxdproject.coder.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {
    
    /**
     * Token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserInfoDTO userInfo;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfoDTO {
        private Long id;
        private String username;
        private String phoneNumber;
        private String avatar;
        private String role;
        private Boolean enabled;
        private String thumbAvatar;
    }
}

