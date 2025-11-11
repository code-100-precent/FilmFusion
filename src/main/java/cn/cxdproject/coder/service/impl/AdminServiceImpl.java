package cn.cxdproject.coder.service.impl;


import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.UserConstants;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.common.storage.FileStorageAdapter;
import cn.cxdproject.coder.config.JwtConfig;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.mapper.AdminMapper;
import cn.cxdproject.coder.model.dto.user.AdminCreateUserRequest;
import cn.cxdproject.coder.model.dto.user.AdminRegisterRequest;
import cn.cxdproject.coder.model.dto.user.UserEmailRequest;
import cn.cxdproject.coder.model.entity.Admin;
import cn.cxdproject.coder.model.vo.UserVO;
import cn.cxdproject.coder.service.AdminService;
import cn.cxdproject.coder.utils.RedisUtils;
import cn.cxdproject.coder.utils.SensitiveDataUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hibiscus.signal.spring.anno.SignalEmitter;
import com.hibiscus.signal.spring.configuration.SignalContextCollector;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.constants.CommonEventConstants.EVENT_INTER_MEDIATE_REQUEST;
import static cn.cxdproject.coder.common.constants.RoleConstants.ADMIN;
import static cn.cxdproject.coder.common.constants.UserConstants.*;
import static cn.cxdproject.coder.common.constants.UserEventConstants.*;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.RATE_LIMIT_EXCEEDED;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.SYSTEM_ERROR;
import static cn.cxdproject.coder.utils.CodeUtil.generateRandomCode;
import static cn.cxdproject.coder.utils.CodeUtil.generateRedisKey;

/**
 * Admin 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {
    private final FileStorageAdapter fileStorageAdapter;

    private final RedisUtils redisUtils;

    private final AdminMapper adminMapper;

    private final JwtConfig jwtConfig;

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public AdminServiceImpl(FileStorageAdapter fileStorageAdapter, RedisUtils redisUtils, AdminMapper adminMapper, JwtConfig jwtConfig) {
        this.fileStorageAdapter = fileStorageAdapter;
        this.redisUtils = redisUtils;
        this.adminMapper = adminMapper;
        this.jwtConfig = jwtConfig;
    }

    /**
     * 管理员直接注册（不需要验证码）
     */
    @Override
    @Transactional
    @SignalEmitter(USER_REGISTER_EVENT)
    public UserVO register(AdminRegisterRequest request, HttpServletRequest httpRequest) {
        String email = request.getEmail();
        String username = request.getUsername();
        String password = request.getPassword();

        // 1. 验证邮箱格式
        if (!SensitiveDataUtils.isValidEmail(email)) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱格式不正确");
        }

        // 2. 检查邮箱是否已被注册
        LambdaQueryWrapper<Admin> emailQueryWrapper = new LambdaQueryWrapper<>();
        emailQueryWrapper.eq(Admin::getEmail, email);
        Admin adminByEmail = adminMapper.selectOne(emailQueryWrapper);
        if (adminByEmail != null) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱已被注册");
        }

        // 3. 检查用户名是否已被使用
        LambdaQueryWrapper<Admin> usernameQueryWrapper = new LambdaQueryWrapper<>();
        usernameQueryWrapper.eq(Admin::getUsername, username);
        Admin adminByUsername = adminMapper.selectOne(usernameQueryWrapper);
        if (adminByUsername != null) {
            throw new BusinessException(SYSTEM_ERROR.code(), "用户名已被使用");
        }

        // 4. 创建管理员实体
        Admin admin = Admin.builder()
                .username(username)
                .email(email)
                .password(getPwdSalt(password, "cetide")) // 加密密码
                .avatar(DEFAULT_USER)
                .role(ADMIN) // 管理员角色
                .enabled(true)
                .build();

        // 5. 保存到数据库
        try {
            if (adminMapper.insert(admin) != 1) {
                throw new BusinessException(SYSTEM_ERROR.code(), "注册失败，请稍后重试");
            }

            // 6. 设置上下文
            SignalContextCollector.collect(EVENT_INTER_MEDIATE_USER, admin);
            SignalContextCollector.collect(EVENT_INTER_MEDIATE_REQUEST, httpRequest);

            // 7. 生成JWT token
            String token = jwtConfig.generateToken(admin);

            // 8. 缓存token和管理员信息
            redisUtils.set(TOKEN_CACHE_KEY + admin.getId(), token, TOKEN_CACHE_TIME);
            redisUtils.set(USER_CACHE_KEY + admin.getEmail(), admin.getEmail(), USER_CACHE_TIME);

            // 9. 返回VO
            UserVO userVO = UserVO.convertToUserVO(admin);
            userVO.setToken(token);
            return userVO;
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR.code(), "注册失败：" + e.getMessage());
        }
    }

    /**
     * 用户邮箱注册
     */
    @Override
    @Transactional
    @SignalEmitter(USER_REGISTER_EVENT)
    public UserVO registerByEmail(UserEmailRequest userRegisterEmailRequest, HttpServletRequest request) {
        String email = userRegisterEmailRequest.getEmail();
        if (!SensitiveDataUtils.isValidEmail(email)) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱格式不正确");
        }
        String redisKeyCode = generateRedisKey(email, SEND_EMAIL_CODE);
        if (Boolean.FALSE.equals(redisUtils.exists(redisKeyCode))) {
            throw new BusinessException(SYSTEM_ERROR.code(), "验证码过期, 请重新发送验证码");
        }
        if (!userRegisterEmailRequest.getCode().equals(redisUtils.get(redisKeyCode))) {
            throw new BusinessException(SYSTEM_ERROR.code(), "验证码错误, 请重新发送验证码");
        }
        LambdaQueryWrapper<Admin> adminLambdaQueryWrapper = new LambdaQueryWrapper<>();
        adminLambdaQueryWrapper.eq(Admin::getEmail, email);
        Admin adminByEmail = adminMapper.selectOne(adminLambdaQueryWrapper);
        if (null != adminByEmail) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱已被注册");
        }
        Admin admin = UserConstants.BuildNewAdmin(userRegisterEmailRequest.getEmail());

        try {
            if (adminMapper.insert(admin) != 1) {
                throw new BusinessException(SYSTEM_ERROR.code(), "注册失败, 请联系站长");
            }
            SignalContextCollector.collect(EVENT_INTER_MEDIATE_USER, admin);
            SignalContextCollector.collect(EVENT_INTER_MEDIATE_REQUEST, request);
            redisUtils.set(USER_CACHE_KEY + admin.getEmail(), admin.getEmail(), USER_CACHE_TIME);
            String token = jwtConfig.generateToken(admin);
            // 缓存token和管理员信息
            redisUtils.set(TOKEN_CACHE_KEY + admin.getId(), token, TOKEN_CACHE_TIME);
            redisUtils.set(USER_CACHE_KEY + admin.getEmail(), admin.getEmail(), USER_CACHE_TIME);
            UserVO userVO = UserVO.convertToUserVO(admin);
            userVO.setToken(token);
            return userVO;
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR.code(), "操作失败, 请联系站长" + e.getMessage());
        }
    }

    @Override
    public Admin getCurrentAdmin() {
        return AuthContext.getCurrentAdmin();
    }

    /**
     * 用户邮箱登录
     */
    @Override
    @Transactional
    @SignalEmitter(USER_LOGIN_EVENT)
    public UserVO loginByEmail(UserEmailRequest userEmailRequest, HttpServletRequest request) {
        String email = userEmailRequest.getEmail();
        if (!SensitiveDataUtils.isValidEmail(email)) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱格式不正确");
        }
        if (!adminMapper.existsByEmail(email)) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱未注册");
        }
        String redisKeyCode = generateRedisKey(email, SEND_EMAIL_CODE);
        if (Boolean.FALSE.equals(redisUtils.exists(redisKeyCode))) {
            throw new BusinessException(SYSTEM_ERROR.code(), "操作失败，请重新发送验证码");
        }
        if (!userEmailRequest.getCode().equals(redisUtils.get(redisKeyCode))) {
            throw new BusinessException(SYSTEM_ERROR.code(), "验证码错误");
        }
        Admin admin = adminMapper.selectByEmail(userEmailRequest.getEmail());
        SignalContextCollector.collect(EVENT_INTER_MEDIATE_USER, admin);
        SignalContextCollector.collect(EVENT_INTER_MEDIATE_REQUEST, request);
        String token = jwtConfig.generateToken(admin);
        // 缓存token和管理员信息
        redisUtils.set(TOKEN_CACHE_KEY + admin.getId(), token, TOKEN_CACHE_TIME);
        redisUtils.set(USER_CACHE_KEY + admin.getEmail(), admin.getEmail(), USER_CACHE_TIME);
        UserVO userVO = UserVO.convertToUserVO(admin);
        userVO.setToken(token);
        return userVO;
    }

    /**
     * 用户密码登录
     */
    @Override
    @Transactional
    @SignalEmitter(USER_LOGIN_EVENT)
    public UserVO loginByPwd(String email, String password) {
        Admin admin = adminMapper.selectByEmail(email);
        if (admin == null) {
            throw new BusinessException(SYSTEM_ERROR.code(), "管理员不存在");
        }
        String saltPwd = getPwdSalt(password, "cetide");
        if(!saltPwd.equals(admin.getPassword())){
            throw new BusinessException(SYSTEM_ERROR.code(), "密码错误");
        }

        SignalContextCollector.collect(EVENT_INTER_MEDIATE_USER, admin);
        SignalContextCollector.collect(ACTION_TYPE_LOGIN, email);
        String token = jwtConfig.generateToken(admin);
        // 缓存token和管理员信息
        redisUtils.set(TOKEN_CACHE_KEY + admin.getId(), token, TOKEN_CACHE_TIME);
        redisUtils.set(USER_CACHE_KEY + admin.getEmail(), admin.getEmail(), USER_CACHE_TIME);
        UserVO userVO = UserVO.convertToUserVO(admin);
        userVO.setToken(token);
        return userVO;
    }

    /**
     * 更新用户信息
     *
     * 这里就有涉及到缓存与数据库一致性的问题了，这里介绍一下不同场景下如何处理这种问题
     *
     * 1. 缓存更新
     *  1.1. 更新完数据库马上就更新缓存
     *  1.2. 更新完缓存，异步的更新数据库（用于需要快速返回响应的场景）
     * 2. 缓存失效策略，在数据库数据更新了之后呢，删除缓存，等待后面查询的时候重建，或者直接更新缓存
     * 3. 延迟双删，在更新了数据库之后，删除缓存，然后时隔50毫秒左右在此进行删除，从而保证脏数据不会被再次出现
     * 4. 使用消息队列进行异步的同步
     * 5. 使用带有时间戳的版本更新方法保证一致性，每次操作了数据都要带上版本号
     * 6. 使用分布式锁的方式来保证一致性
     */
    @Override
    @Transactional
    @Loggable(type = LogType.USER_UPDATE, value = "管理员修改信息")
    public String updateAdminInfo(Admin admin) {
        if (admin == null) {
            throw new IllegalArgumentException("error.username.empty");
        }

        Admin existingAdmin = getCurrentAdmin();
        if (existingAdmin == null) {
            throw new BusinessException(SYSTEM_ERROR.code(), "当前管理员不存在");
        }
        // 验证当前管理员是否有权限修改
        Admin currentAdmin = getAdminById(existingAdmin.getId());
        if (!Objects.equals(currentAdmin.getId(), existingAdmin.getId())) {
            throw new IllegalArgumentException("error.admin.permission.denied");
        }
        // 确保 ID 不被修改
        admin.setId(existingAdmin.getId());
        // 防止敏感字段被修改
        admin.setPassword(existingAdmin.getPassword());

        // 使用 ID 查询管理员信息
        Admin updatedAdmin = adminMapper.selectById(admin.getId());
        if (updatedAdmin == null) {
            throw new BusinessException(SYSTEM_ERROR.code(), "管理员不存在");
        }

        // 删除旧缓存
        redisUtils.delete(USER_CACHE_KEY + updatedAdmin.getId());

        // 延时删除缓存
        scheduler.schedule(() -> {
            redisUtils.delete(USER_CACHE_KEY + updatedAdmin.getId());
        }, 500, TimeUnit.MILLISECONDS);

        // 更新数据库
        int updateCount = adminMapper.updateById(admin);
        if (updateCount == 0) {
            throw new BusinessException(SYSTEM_ERROR.code(), "更新失败");
        }

        // 清除缓存并重新添加
        redisUtils.delete(USER_CACHE_KEY + admin.getId());
        redisUtils.set(USER_CACHE_KEY + admin.getId(), admin.getId(), USER_CACHE_TIME);
        String token = jwtConfig.generateToken(admin);
        // 缓存token和管理员信息
        redisUtils.set(TOKEN_CACHE_KEY + admin.getId(), token, TOKEN_CACHE_TIME);
        redisUtils.set(USER_CACHE_KEY + admin.getId(), admin.getId(), USER_CACHE_TIME);
        return token;
    }

    /**
     * 用户退出登录
     */
    @Override
    @Loggable(type = LogType.USER_LOGOUT, value = "管理员退出登录")
    public void logoutAdmin(String currentToken) {
        redisUtils.delete(TOKEN_CACHE_KEY + jwtConfig.getAdminFromToken(currentToken).getId());
    }

    /**
     * 上传用户头像
     */
    @Override
    @Loggable(type = LogType.USER_UPDATE, value = "上传头像")
    public String uploadAvatar(MultipartFile file) {
        try {
            // 使用新的文件上传服务（上传到tmp/avatar目录）
            String upload = fileStorageAdapter.upload("avatar", file);
            Admin admin = adminMapper.selectById(AuthContext.getCurrentAdmin().getId());
            
            // 删除旧头像（如果存在且不是默认头像）
            if (admin.getAvatar() != null && !admin.getAvatar().equals(DEFAULT_USER) && !admin.getAvatar().startsWith("http")) {
                try {
                    fileStorageAdapter.delete(admin.getAvatar());
                } catch (Exception e) {
                    // 忽略删除失败，继续执行
                }
            }
            
            // 返回访问URL（相对路径）
            String avatarUrl = "/api/files/avatar/" + upload.substring(upload.lastIndexOf("/") + 1);
            admin.setAvatar(avatarUrl);
            adminMapper.updateById(admin);
            
            // 删除旧缓存
            redisUtils.delete(USER_CACHE_KEY + admin.getEmail());

            // 延时删除缓存
            scheduler.schedule(() -> {
                redisUtils.delete(USER_CACHE_KEY + admin.getEmail());
            }, 500, TimeUnit.MILLISECONDS);
            
            return avatarUrl;
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR.code(), "头像上传失败: " + e.getMessage());
        }
    }

    /**
     * 用户申请注销账号
     */
    @Override
    @Transactional
    @Loggable(type = LogType.USER_DELETE, value = "注销管理员")
    public void requestAccountDeletion() {
        Admin admin = adminMapper.selectById(AuthContext.getCurrentAdmin().getId());
        if (admin == null) {
            throw new IllegalArgumentException("管理员不存在");
        }
        admin.setDeleted(true);
        adminMapper.updateById(admin);
    }

    /**
     * 通过 Email 获取管理员信息
     */
    @Override
    public Admin getAdminByEmail(String email) {
        // 使用 LambdaQueryWrapper 来构建查询条件
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getEmail, email);

        // 执行查询
        Admin admin = this.getOne(queryWrapper);

        // 如果未找到管理员，返回 null 或抛出异常
        if (admin == null) {
            throw new BusinessException(SYSTEM_ERROR.code(), "管理员不存在");
        }

        return admin;
    }

    /**
     * 修改密码
     */
    @Override
    public String changePassword(String oldPassword, String newPassword) {
        Admin admin = getCurrentAdmin();
        Admin currentAdmin = adminMapper.selectById(admin.getId());
        System.out.println("当前old密码:"+oldPassword);
        System.out.println("当前密码:"+currentAdmin.getPassword());
        System.out.println("当前盐值密码:"+getPwdSalt(oldPassword, "cetide"));
        System.out.println("修改的new密码"+newPassword);

        //加密
        String saltPwd = getPwdSalt(oldPassword, "cetide");
        if (!currentAdmin.getPassword().equals(saltPwd)) {
            return "原密码输入错误";
        }
        // 设置新密码
        String newPwd = getPwdSalt(newPassword, "cetide");
        currentAdmin.setPassword(newPwd);
        adminMapper.updateById(currentAdmin);
        return "密码修改成功";
    }

    private String getPwdSalt(String password,String salt) {
        return DigestUtils.md5Hex(password + salt).toUpperCase();
    }

    /**
     * 第三方平台登录
     */
    @Override
    public Admin createAdminFromSocialLogin(String providerUsername, String providerEmail, String avatarUrl) {
        Admin admin = UserConstants.BuildNewAdmin(providerUsername, providerEmail, avatarUrl);
        try {
            if (adminMapper.insert(admin) != 1) {
                throw new BusinessException(SYSTEM_ERROR.code(), "注册失败, 请联系站长");
            }
            // 缓存管理员信息
            redisUtils.set(USER_CACHE_KEY + admin.getId(), admin, USER_CACHE_TIME);
            redisUtils.set(USER_CACHE_KEY + admin.getUsername(), admin, USER_CACHE_TIME);
            return admin;
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR.code(), "操作失败, 请联系站长");
        }
    }

    /**
     * 根据ID获取管理员信息
     */
    @Override
    public Admin getAdminById(Long id) {
        // 先从缓存获取
        redisUtils.delete(USER_CACHE_KEY + id);
        Object cached = redisUtils.get(USER_CACHE_KEY + id);
        if (cached != null) {
            return (Admin) cached;
        }

        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(SYSTEM_ERROR.code(), "管理员不存在");
        }

        // 写入缓存
        redisUtils.set(USER_CACHE_KEY + id, admin, USER_CACHE_TIME);
        return admin;
    }

    /**
     * 管理员登录
     */
    @Override
    @Transactional
    public UserVO adminLoginByEmail(UserEmailRequest userEmailRequest, HttpServletRequest request) {
        String email = userEmailRequest.getEmail();
        if (!SensitiveDataUtils.isValidEmail(email)) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱格式不正确");
        }
        if (!adminMapper.existsByEmail(email)) {
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱未注册");
        }
        String redisKeyCode = generateRedisKey(email, SEND_EMAIL_CODE);
        if (Boolean.FALSE.equals(redisUtils.exists(redisKeyCode))) {
            throw new BusinessException(SYSTEM_ERROR.code(), "操作失败，请重新发送验证码");
        }
        if (!userEmailRequest.getCode().equals(redisUtils.get(redisKeyCode))) {
            throw new BusinessException(SYSTEM_ERROR.code(), "验证码错误");
        }
        Admin admin = adminMapper.selectByEmail(userEmailRequest.getEmail());
        SignalContextCollector.collect(EVENT_INTER_MEDIATE_USER, admin);
        SignalContextCollector.collect(EVENT_INTER_MEDIATE_REQUEST, request);
        String token = jwtConfig.generateToken(admin);
        // 缓存token和管理员信息
        redisUtils.set(TOKEN_CACHE_KEY + admin.getId(), token, TOKEN_CACHE_TIME);
        redisUtils.set(USER_CACHE_KEY + admin.getEmail(), admin.getEmail(), USER_CACHE_TIME);
        UserVO userVO = UserVO.convertToUserVO(admin);
        userVO.setToken(token);
        return userVO;
    }

    //通过邮箱修改密码
    @Override
    public String setPasswordByEmail(UserEmailRequest userEmailRequest,String newPassword) {
        String email = userEmailRequest.getEmail();
        if (!SensitiveDataUtils.isValidEmail(email))
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱格式不正确");

        if (!adminMapper.existsByEmail(email))
            throw new BusinessException(SYSTEM_ERROR.code(), "邮箱未注册");

        String redisKeyCode = generateRedisKey(email, SEND_EMAIL_CODE);
        if (Boolean.FALSE.equals(redisUtils.exists(redisKeyCode)))
            throw new BusinessException(SYSTEM_ERROR.code(), "操作失败，请重新发送验证码");

        if (!userEmailRequest.getCode().equals(redisUtils.get(redisKeyCode))) {
            throw new BusinessException(SYSTEM_ERROR.code(), "验证码错误");
        }
        Admin admin = adminMapper.selectByEmail(userEmailRequest.getEmail());
        String saltPwd = getPwdSalt(newPassword, "cetide");
        admin.setPassword(saltPwd);
        adminMapper.updateById(admin);
        return "修改成功";
    }

    /**
     * 管理员删除管理员
     */
    @Override
    public boolean deleteAdminByAdmin(Long adminId) {
        return adminMapper.deleteById(adminId) > 0;
    }

    /**
     * 管理员批量删除管理员
     */
    @Override
    public boolean batchDeleteAdmins(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return true;
        return adminMapper.deleteBatchIds(ids) > 0;
    }

    /**
     * 启用/禁用管理员
     */
    @Override
    public boolean updateAdminStatus(Long adminId, boolean enabled) {
        Admin a = adminMapper.selectById(adminId);
        if (a == null) return false;
        a.setEnabled(enabled);
        return adminMapper.updateById(a) > 0;
    }

    /**
     * 重置密码
     */
    @Override
    public boolean resetPasswordByAdmin(Long adminId, String rawPassword) {
        Admin a = adminMapper.selectById(adminId);
        if (a == null) return false;
        String saltPwd = getPwdSalt(rawPassword, "cetide");
        a.setPassword(saltPwd);
        return adminMapper.updateById(a) > 0;
    }
}
