package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.LoginResponseDTO;
import cn.cxdproject.coder.model.dto.RegisterDTO;
import cn.cxdproject.coder.model.dto.UpdateUserDTO;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.model.vo.UserVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * User 服务接口
 * 定义用户认证与管理相关能力
 * @author Hibiscus-code-generate
 */
public interface UserService extends IService<User> {
    
    /**
     * 用户登录
     * @param username 用户名或手机号
     * @param password 密码
     * @return 登录响应（包含token和用户信息）
     */
    LoginResponseDTO login(String username, String password);
    
    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return 登录响应（包含token和用户信息）
     */
    LoginResponseDTO register(RegisterDTO registerDTO);
    
    /**
     * 根据用户名或手机号查找用户
     * @param username 用户名或手机号
     * @return 用户实体
     */
    User findByUsernameOrPhone(String username);
    
    /**
     * 更新用户信息（用户自己更新）
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 更新后的用户VO
     */
    UserVO updateUserInfo(Long userId, UpdateUserDTO updateDTO);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 获取当前用户信息
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getCurrentUserInfo(Long userId);
    
    /**
     * 检查是否是管理员
     * @param userId 用户ID
     * @return 是否是管理员
     */
    boolean isAdmin(Long userId);
    
    /**
     * 管理员创建用户
     * @param user 用户实体
     * @return 用户VO
     */
    UserVO createUserByAdmin(User user);
    
    /**
     * 管理员更新用户
     * @param userId 用户ID
     * @param user 用户实体
     * @return 用户VO
     */
    UserVO updateUserByAdmin(Long userId, User user);
    
    /**
     * 管理员删除用户（逻辑删除）
     * @param userId 用户ID
     */
    void deleteUserByAdmin(Long userId);
    
    /**
     * 管理员分页查询用户
     * @param page 分页对象
     * @param keyword 关键字（用户名或手机号）
     * @return 分页结果
     */
    Page<UserVO> getUserPageByAdmin(Page<User> page, String keyword);
    
    /**
     * 管理员根据ID获取用户
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUserByIdByAdmin(Long userId);
    
    /**
     * 将User实体转换为UserVO
     * @param user 用户实体
     * @return 用户VO
     */
    UserVO toUserVO(User user);

    /**
     * 将User实体转换为UserVO（不对电话号码进行敏感处理）
     * @param user 用户实体
     * @return 用户VO（包含完整电话号码）
     */
    UserVO toUserAdminVO(User user);
    
    /**
     * 上传用户头像
     * @param userId 用户ID
     * @param avatarFile 头像文件
     * @return 头像URL
     */
    FileVO uploadAvatar(Long userId, org.springframework.web.multipart.MultipartFile avatarFile);
}
