package cn.cxdproject.coder.service;


import cn.cxdproject.coder.model.dto.user.AdminRegisterRequest;
import cn.cxdproject.coder.model.dto.user.UserEmailRequest;
import cn.cxdproject.coder.model.entity.Admin;
import cn.cxdproject.coder.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Admin 服务接口
 * @author Hibiscus-code-generate
 */
public interface AdminService extends IService<Admin> {

    /**
     * 处理邮箱注册逻辑（需要验证码）
     */
    UserVO registerByEmail(UserEmailRequest adminRegisterEmailRequest, HttpServletRequest request);

    /**
     * 管理员直接注册（不需要验证码）
     */
    UserVO register(AdminRegisterRequest request, HttpServletRequest httpRequest);

    /**
     * 获取当前登录管理员
     */
    Admin getCurrentAdmin();

    /**
     * 管理员登录
     */
    UserVO loginByEmail(UserEmailRequest adminEmailRequest, HttpServletRequest request);

    /**
     * 修改管理员信息
     */
    String updateAdminInfo(Admin admin);

    /**
     * 管理员退出登录
     */
    void logoutAdmin(String currentToken);

    /**
     * 管理员上传头像
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 管理员申请注销账号
     */
    void requestAccountDeletion();

    /**
     * 根据邮箱获取管理员信息
     */
    Admin getAdminByEmail(String email);

    /**
     * 修改密码
     */
    String changePassword(String oldPassword, String newPassword);

    /**
     * 根据github信息创建新管理员
     */
    Admin createAdminFromSocialLogin(String providerUsername, String providerEmail, String avatarUrl);

    /**
     * 根据id获取管理员
     */
    Admin getAdminById(Long id);

    /**
     * 管理员登录
     */
    UserVO adminLoginByEmail(UserEmailRequest adminEmailRequest, HttpServletRequest request);

    /**
     * 通过邮箱修改密码
     */
    String setPasswordByEmail(UserEmailRequest adminEmailRequest, String newPassword);

    /**
     * 管理员删除管理员
     */
    boolean deleteAdminByAdmin(Long adminId);

    /**
     * 管理员批量删除管理员
     */
    boolean batchDeleteAdmins(List<Long> ids);

    /**
     * 启用/禁用管理员
     */
    boolean updateAdminStatus(Long adminId, boolean enabled);

    /**
     * 重置密码
     */
    boolean resetPasswordByAdmin(Long adminId, String rawPassword);

    UserVO loginByPwd(String email, String password);
}

