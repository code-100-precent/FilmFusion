package cn.cxdproject.coder.model.vo;

import cn.cxdproject.coder.model.entity.Admin;
import lombok.Data;

@Data
public class UserVO {
    private Long id;

    private String username;

    private String email;

    private String avatar;

    private String token;

    private String role;


    public UserVO() {
    }


    public static UserVO convertToUserVO(Admin admin){
        UserVO userVO = new UserVO();
        userVO.setId(admin.getId());
        userVO.setUsername(admin.getUsername());
        userVO.setEmail(admin.getEmail());
        userVO.setAvatar(admin.getAvatar());
        userVO.setRole(admin.getRole());

        return userVO;
    }
}
