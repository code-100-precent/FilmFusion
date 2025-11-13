package cn.cxdproject.coder.model.dto.user;

import lombok.Data;

@Data
public class AdminCreateUserRequest {
    private String username;
    private String email;
    private String avatar;
    private String phone;
}
