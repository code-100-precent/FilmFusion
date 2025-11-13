package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录请求DTO
 * 
 * @author heathcetide
 */
@Data
public class LoginDTO {
    
    /**
     * 用户名或手机号
     */
    @NotBlank(message = "用户名或手机号不能为空")
    private String username;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;
}

