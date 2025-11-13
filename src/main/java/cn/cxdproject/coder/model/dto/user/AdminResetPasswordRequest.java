package cn.cxdproject.coder.model.dto.user;

import lombok.Data;

@Data
public class AdminResetPasswordRequest {
    private String newPassword;
}