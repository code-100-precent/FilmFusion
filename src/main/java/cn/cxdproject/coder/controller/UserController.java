package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.*;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.UserVO;
import cn.cxdproject.coder.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * 用户控制器
 * 
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @PublicAccess
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginResponseDTO response = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        return ApiResponse.success(response);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @PublicAccess
    public ApiResponse<LoginResponseDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        LoginResponseDTO response = userService.register(registerDTO);
        return ApiResponse.success(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public ApiResponse<UserVO> getCurrentUserInfo() {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        UserVO userVO = userService.getCurrentUserInfo(currentUser.getId());
        return ApiResponse.success(userVO);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/info")
    public ApiResponse<UserVO> updateCurrentUserInfo(@Valid @RequestBody UpdateUserDTO updateDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        UserVO userVO = userService.updateUserInfo(currentUser.getId(), updateDTO);
        return ApiResponse.success(userVO);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        userService.changePassword(
                currentUser.getId(),
                changePasswordDTO.getOldPassword(),
                changePasswordDTO.getNewPassword()
        );
        return ApiResponse.success();
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        String avatarUrl = userService.uploadAvatar(currentUser.getId(), file);
        return ApiResponse.success(avatarUrl);
    }

    /**
     * 检查是否是管理员
     */
    @GetMapping("/is-admin")
    public ApiResponse<Boolean> isAdmin() {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        boolean isAdmin = userService.isAdmin(currentUser.getId());
        return ApiResponse.success(isAdmin);
    }

    // ==================== 管理员接口 ====================

    /**
     * 管理员创建用户
     */
    @PostMapping("/admin/create")
    public ApiResponse<UserVO> createUserByAdmin(@Valid @RequestBody User user) {
        // 权限检查在拦截器中完成
        UserVO userVO = userService.createUserByAdmin(user);
        return ApiResponse.success(userVO);
    }

    /**
     * 管理员更新用户
     */
    @PutMapping("/admin/{userId}")
    public ApiResponse<UserVO> updateUserByAdmin(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId,
            @Valid @RequestBody User user) {
        // 权限检查在拦截器中完成
        UserVO userVO = userService.updateUserByAdmin(userId, user);
        return ApiResponse.success(userVO);
    }

    /**
     * 管理员删除用户（逻辑删除）
     */
    @DeleteMapping("/admin/{userId}")
    public ApiResponse<Void> deleteUserByAdmin(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId) {
        // 权限检查在拦截器中完成
        userService.deleteUserByAdmin(userId);
        return ApiResponse.success();
    }

    /**
     * 管理员分页查询用户
     */
    @GetMapping("/admin/page")
    public ApiResponse<PageResponse<UserVO>> getUserPageByAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        // 权限检查在拦截器中完成
        Page<User> page = new Page<>(current, size);
        Page<UserVO> userPage = userService.getUserPageByAdmin(page, keyword);
        return ApiResponse.success(PageResponse.of(
                (int) userPage.getCurrent(),
                (int) userPage.getSize(),
                userPage.getTotal(),
                userPage.getRecords()
        ));
    }

    /**
     * 管理员根据ID获取用户详情
     */
    @GetMapping("/admin/{userId}")
    public ApiResponse<UserVO> getUserByIdByAdmin(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId) {
        // 权限检查在拦截器中完成
        UserVO userVO = userService.getUserByIdByAdmin(userId);
        return ApiResponse.success(userVO);
    }
}
