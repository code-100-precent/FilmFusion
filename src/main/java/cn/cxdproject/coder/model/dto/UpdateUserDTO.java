package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 更新用户信息DTO
 * 
 * @author heathcetide
 */
@Data
public class UpdateUserDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    
    /**
     * 头像
     */
    private String avatar;
}

