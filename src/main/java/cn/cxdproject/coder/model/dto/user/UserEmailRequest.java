package cn.cxdproject.coder.model.dto.user;

import lombok.Data;

@Data
public class UserEmailRequest {

    private String email;

    private String code;
}
