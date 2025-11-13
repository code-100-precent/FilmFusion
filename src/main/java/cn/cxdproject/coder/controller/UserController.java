package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * User 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


}
