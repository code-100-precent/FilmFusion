package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.user.AdminRegisterRequest;
import cn.cxdproject.coder.model.dto.user.UserEmailRequest;
import cn.cxdproject.coder.model.entity.Admin;
import cn.cxdproject.coder.model.vo.UserVO;
import cn.cxdproject.coder.service.AdminService;
import cn.cxdproject.coder.utils.RedisUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.SYSTEM_ERROR;

/**
 * 管理员模块
 *
 * @author heathcetide
 */
@RestController
@RequestMapping("/api/admin")
@Api(tags = "管理员模块")
public class AdminController {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    /**
     * 管理员模块
     */
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }


    /**
     * 管理员直接注册（不需要验证码）
     */
    @PostMapping("/register")
    @ApiOperation("管理员注册")
    @PublicAccess
    public ApiResponse<UserVO> register(@Valid @RequestBody AdminRegisterRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(adminService.register(request, httpRequest));
    }

    /**
     * 邮箱注册账号（需要验证码，保留兼容）
     */
    @PostMapping("/register/email")
    @ApiOperation("邮箱注册账号")
    @PublicAccess
    public ApiResponse<UserVO> registerEmail(@RequestBody UserEmailRequest adminEmailRequest, HttpServletRequest request) {
        if (adminEmailRequest.getCode().isEmpty() || adminEmailRequest.getEmail().isEmpty()) {
            return ApiResponse.error("注册信息不全");
        }
        return ApiResponse.success(adminService.registerByEmail(adminEmailRequest, request));
    }

    /**
     * 邮箱登录（需要验证码）
     */
    @PostMapping("/login/email")
    @ApiOperation("邮箱登录账号")
    @PublicAccess
    public ApiResponse<UserVO> loginByEmail(@RequestBody UserEmailRequest adminEmailRequest, HttpServletRequest request) {
        if (adminEmailRequest.getCode().isEmpty() || adminEmailRequest.getEmail().isEmpty()) {
            return ApiResponse.error("登录信息不全");
        }
        return ApiResponse.success(adminService.loginByEmail(adminEmailRequest, request));
    }

    /**
     * 密码登录
     */
    @PostMapping("/login/password")
    @ApiOperation("密码登录账号")
    @PublicAccess
    public ApiResponse<UserVO> loginByPwd(@RequestParam("email") String email, @RequestParam("password") String password) {
        return ApiResponse.success(adminService.loginByPwd(email, password));
    }

    /**
     * 根据token获取信息
     */
    @GetMapping("/info")
    @ApiOperation("根据token获取角色信息")
    public ApiResponse<UserVO> getAdminInfoByToken() {
        Admin currentAdmin = adminService.getCurrentAdmin();
        if (currentAdmin != null) {
            currentAdmin.setPassword(null);
            return ApiResponse.success(UserVO.convertToUserVO(currentAdmin));
        } else {
            return ApiResponse.error(SYSTEM_ERROR.code(), "管理员不存在");
        }
    }

    /**
     * 根据AdminId获取信息
     */
    @GetMapping("/getAdminInfoByAdminId")
    @ApiOperation("根据AdminId获取角色信息")
    public ApiResponse<UserVO> getAdminInfoById(@RequestParam("adminId") Long id) {
        Admin admin = adminService.getAdminById(id);
        if (admin != null) {
            admin.setPassword(null);
            return ApiResponse.success(UserVO.convertToUserVO(admin));
        } else {
            return ApiResponse.error(SYSTEM_ERROR.code(), "管理员不存在");
        }
    }

    /**
     * 根据Email获取信息
     */
    @GetMapping("/getAdminInfoByEmail")
    @ApiOperation("根据Email获取角色信息")
    public ApiResponse<UserVO> getAdminInfoByEmail(@RequestParam("email") String email) {
        Admin admin = adminService.getAdminByEmail(email);
        if (admin != null) {
            admin.setPassword(null);
            return ApiResponse.success(UserVO.convertToUserVO(admin));
        } else {
            return ApiResponse.error(SYSTEM_ERROR.code(), "管理员不存在");
        }
    }


    /**
     * 管理员退出登录
     */
    @PostMapping("/logout")
    @ApiOperation("管理员退出登录")
    public ApiResponse<String> logout() {
        String currentToken = AuthContext.getCurrentToken();
        adminService.logoutAdmin(currentToken);
        return ApiResponse.success("退出登录成功");
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload-avatar")
    @ApiOperation("上传头像")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }
            String avatarUrl = adminService.uploadAvatar(file);
            return ApiResponse.success(avatarUrl);
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return ApiResponse.error("头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 普通管理员修改自己的信息
     */
    @PutMapping("/update")
    @ApiOperation("普通管理员修改自己的信息")
    public ApiResponse<String> updateAdmin(@RequestBody Admin admin) {
        try {
            return ApiResponse.success(adminService.updateAdminInfo(admin));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(SYSTEM_ERROR.code(), e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error occurred while updating admin info", e);
            return ApiResponse.error(SYSTEM_ERROR.code(), "服务器内部错误");
        }
    }

    /**
     * 注销管理员
     */
    @PostMapping("/delete-account")
    @ApiOperation("注销管理员")
    public ApiResponse<Void> deleteAccount() {
        adminService.requestAccountDeletion();
        return ApiResponse.success(null);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @ApiOperation("修改密码")
    public ApiResponse<String> changePassword(@RequestParam("oldPassword") String oldPwd, @RequestParam("newPassword") String newPwd) {
        return ApiResponse.success(adminService.changePassword(oldPwd, newPwd));
    }

    /**
     * 通过邮箱修改密码
     */
    @PostMapping("/set-password-by-email")
    @ApiOperation("通过邮箱修改密码")
    public ApiResponse<String> setPasswordByEmail(@RequestBody UserEmailRequest adminEmailRequest, @RequestParam("newPassword") String newPassword) {
        return ApiResponse.success(adminService.setPasswordByEmail(adminEmailRequest, newPassword));
    }
}
