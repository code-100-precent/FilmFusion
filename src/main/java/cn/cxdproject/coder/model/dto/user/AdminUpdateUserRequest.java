package cn.cxdproject.coder.model.dto.user;

import lombok.Data;

@Data
public class AdminUpdateUserRequest {
    private String username;
    private String avatar;
    private String phone;
    private String bio;
}