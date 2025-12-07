package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.encrypt.PasswordEncryptionService;
import cn.cxdproject.coder.common.storage.FileStorageAdapter;
import cn.cxdproject.coder.config.JwtConfig;
import cn.cxdproject.coder.exception.AuthException;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.LoginResponseDTO;
import cn.cxdproject.coder.model.dto.RegisterDTO;
import cn.cxdproject.coder.model.dto.UpdateUserDTO;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.model.vo.UserVO;
import cn.cxdproject.coder.mapper.UserMapper;
import cn.cxdproject.coder.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.constants.RoleConstants.ADMIN;
import static cn.cxdproject.coder.common.constants.RoleConstants.USER;
import static cn.cxdproject.coder.common.constants.UserConstants.DEFAULT_THUMB_USER;
import static cn.cxdproject.coder.common.constants.UserConstants.DEFAULT_USER;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * User 服务实现类
 * @author Hibiscus-code-generate
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final PasswordEncryptionService passwordEncryptionService;
    private final JwtConfig jwtConfig;
    private final FileStorageAdapter fileStorageAdapter;

    public UserServiceImpl(PasswordEncryptionService passwordEncryptionService, JwtConfig jwtConfig, FileStorageAdapter fileStorageAdapter) {
        this.passwordEncryptionService = passwordEncryptionService;
        this.jwtConfig = jwtConfig;
        this.fileStorageAdapter = fileStorageAdapter;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO login(String username, String password) {
        // 查找用户
        User user = findByUsernameOrPhone(username);
        if (user == null) {
            throw new AuthException(UNAUTHORIZED.code(), "用户名或密码错误");
        }

        // 检查用户是否被删除
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new AuthException(UNAUTHORIZED.code(), "用户已被删除");
        }

        // 检查用户是否被禁用
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new AuthException(UNAUTHORIZED.code(), "用户已被禁用");
        }

        // 验证密码
        log.debug("验证密码 - 用户ID: {}, 用户名: {}, 密码hash: {}", user.getId(), user.getUsername(), user.getPassword());
        boolean passwordMatches = passwordEncryptionService.matchesBCrypt(password, user.getPassword());
        log.debug("密码验证结果: {}", passwordMatches);
        if (!passwordMatches) {
            log.warn("密码验证失败 - 用户ID: {}, 用户名: {}", user.getId(), user.getUsername());
            throw new AuthException(UNAUTHORIZED.code(), "用户名或密码错误");
        }

        // 生成token
        String token = jwtConfig.generateToken(user);

        // 构建响应
        return LoginResponseDTO.builder()
                .token(token)
                .userInfo(LoginResponseDTO.UserInfoDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .phoneNumber(user.getPhoneNumber())
                        .avatar(user.getAvatar())
                        .role(user.getRole())
                        .enabled(user.getEnabled())
                        .thumbAvatar(user.getThumbAvatar())
                        .build())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponseDTO register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", registerDTO.getUsername());
        usernameWrapper.eq("deleted", false);
        if (this.count(usernameWrapper) > 0) {
            throw new BusinessException(DUPLICATE_RESOURCE.code(), "用户名已存在");
        }

        // 检查手机号是否已存在
        QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
        phoneWrapper.eq("phoneNumber", registerDTO.getPhoneNumber());
        phoneWrapper.eq("deleted", false);
        if (this.count(phoneWrapper) > 0) {
            throw new BusinessException(DUPLICATE_RESOURCE.code(), "手机号已被注册");
        }

        // 创建新用户
        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncryptionService.encodeWithBCrypt(registerDTO.getPassword()))
                .phoneNumber(registerDTO.getPhoneNumber())
                .avatar(DEFAULT_USER)// 设置默认头像
                .thumbAvatar(DEFAULT_THUMB_USER)
                .role(USER) // 默认角色为普通用户
                .enabled(true)
                .build();

        // 保存用户
        this.save(user);

        // 生成token
        String token = jwtConfig.generateToken(user);

        // 构建响应
        return LoginResponseDTO.builder()
                .token(token)
                .userInfo(LoginResponseDTO.UserInfoDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .phoneNumber(user.getPhoneNumber())
                        .avatar(user.getAvatar())
                        .role(user.getRole())
                        .enabled(user.getEnabled())
                        .thumbAvatar(user.getThumbAvatar())
                        .build())
                .build();
    }

    @Override
    public User findByUsernameOrPhone(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("username", username).or().eq("phoneNumber", username));
        wrapper.eq("deleted", false);
        return this.getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUserInfo(Long userId, UpdateUserDTO updateDTO) {
        // 获取用户
        User user = this.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "用户不存在");
        }

        // 如果更新用户名，检查是否重复
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(user.getUsername())) {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", updateDTO.getUsername());
            wrapper.eq("deleted", false);
            wrapper.ne("id", userId);
            if (this.count(wrapper) > 0) {
                throw new BusinessException(DUPLICATE_RESOURCE.code(), "用户名已存在");
            }
            user.setUsername(updateDTO.getUsername());
        }

        // 如果更新手机号，检查是否重复
        if (updateDTO.getPhoneNumber() != null && !updateDTO.getPhoneNumber().equals(user.getPhoneNumber())) {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("phoneNumber", updateDTO.getPhoneNumber());
            wrapper.eq("deleted", false);
            wrapper.ne("id", userId);
            if (this.count(wrapper) > 0) {
                throw new BusinessException(DUPLICATE_RESOURCE.code(), "手机号已被注册");
            }
            user.setPhoneNumber(updateDTO.getPhoneNumber());
        }

        // 更新头像
        if (updateDTO.getAvatar() != null) {
            user.setAvatar(updateDTO.getAvatar());
        }

        // 保存更新
        this.updateById(user);

        return toUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        // 获取用户
        User user = this.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncryptionService.matchesBCrypt(oldPassword, user.getPassword())) {
            throw new AuthException(UNAUTHORIZED.code(), "旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncryptionService.encodeWithBCrypt(newPassword));
        this.updateById(user);
    }

    @Override
    public UserVO getCurrentUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public boolean isAdmin(Long userId) {
        User user = this.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            return false;
        }
        return ADMIN.equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUserByAdmin(User user) {
        // 检查用户名是否已存在
        QueryWrapper<User> usernameWrapper = new QueryWrapper<>();
        usernameWrapper.eq("username", user.getUsername());
        usernameWrapper.eq("deleted", false);
        if (this.count(usernameWrapper) > 0) {
            throw new BusinessException(DUPLICATE_RESOURCE.code(), "用户名已存在");
        }

        // 检查手机号是否已存在
        if (user.getPhoneNumber() != null) {
            QueryWrapper<User> phoneWrapper = new QueryWrapper<>();
            phoneWrapper.eq("phoneNumber", user.getPhoneNumber());
            phoneWrapper.eq("deleted", false);
            if (this.count(phoneWrapper) > 0) {
                throw new BusinessException(DUPLICATE_RESOURCE.code(), "手机号已被注册");
            }
        }

        // 加密密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncryptionService.encodeWithBCrypt(user.getPassword()));
        }

        // 设置默认值
        if (user.getRole() == null) {
            user.setRole(USER);
        }
        if (user.getEnabled() == null) {
            user.setEnabled(true);
        }
        user.setDeleted(false);

        // 保存用户
        this.save(user);

        return toUserVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUserByAdmin(Long userId, User user) {
        // 获取原用户
        User existingUser = this.getById(userId);
        if (existingUser == null || Boolean.TRUE.equals(existingUser.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "用户不存在");
        }

        // 如果更新用户名，检查是否重复
        if (user.getUsername() != null && !user.getUsername().equals(existingUser.getUsername())) {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("username", user.getUsername());
            wrapper.eq("deleted", false);
            wrapper.ne("id", userId);
            if (this.count(wrapper) > 0) {
                throw new BusinessException(DUPLICATE_RESOURCE.code(), "用户名已存在");
            }
            existingUser.setUsername(user.getUsername());
        }

        // 如果更新手机号，检查是否重复
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.eq("phoneNumber", user.getPhoneNumber());
            wrapper.eq("deleted", false);
            wrapper.ne("id", userId);
            if (this.count(wrapper) > 0) {
                throw new BusinessException(DUPLICATE_RESOURCE.code(), "手机号已被注册");
            }
            existingUser.setPhoneNumber(user.getPhoneNumber());
        }

        // 更新其他字段
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getEnabled() != null) {
            existingUser.setEnabled(user.getEnabled());
        }

        // 如果更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncryptionService.encodeWithBCrypt(user.getPassword()));
        }

        // 保存更新
        this.updateById(existingUser);

        return toUserVO(existingUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserByAdmin(Long userId) {
        User user = this.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "用户不存在");
        }

        // 逻辑删除
        user.setDeleted(true);
        this.updateById(user);

        // 使该用户的所有token失效
        jwtConfig.invalidateToken(jwtConfig.generateToken(user));
    }

    @Override
    public Page<UserVO> getUserPageByAdmin(Page<User> page, String keyword) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        // 关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword).or().like("phoneNumber", keyword));
        }

        // 按创建时间倒序
        wrapper.orderByDesc("created_at");

        // 分页查询
        Page<User> userPage = this.page(page, wrapper);

        // 转换为VO
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public UserVO getUserByIdByAdmin(Long userId) {
        User user = this.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public UserVO toUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        // 电话号码脱敏在UserVO的getter方法中处理
        vo.setPhoneNumber(user.getPhoneNumber());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileVO uploadAvatar(Long userId, MultipartFile avatarFile) {
        // 验证文件
        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new BusinessException(VALIDATION_ERROR.code(), "头像文件不能为空");
        }

        // 验证文件类型
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(VALIDATION_ERROR.code(), "只能上传图片文件");
        }

        // 验证文件大小（限制5MB）
        if (avatarFile.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(VALIDATION_ERROR.code(), "头像文件大小不能超过5MB");
        }

        // 获取用户
        User user = this.getById(userId);
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "用户不存在");
        }

        // 删除旧头像（如果不是默认头像）
        if (user.getAvatar() != null && !user.getAvatar().equals(DEFAULT_USER) && !user.getAvatar().startsWith("http")) {
            try {
                fileStorageAdapter.delete(user.getAvatar());
            } catch (Exception e) {
                log.warn("删除旧头像失败: {}", user.getAvatar(), e);
            }
        }

        // 上传新头像
        FileVO fileVO = fileStorageAdapter.upload("avatars", avatarFile);
        
        // fileVO.getOriginUrl() 返回的是 /files/... 格式（不包含/api前缀，因为前端baseURL已包含/api）
        String originUrl = fileVO.getOriginUrl();
        String thumbUrl = fileVO.getThumbUrl();

        fileVO.setOriginUrl(originUrl);
        fileVO.setThumbUrl(thumbUrl);

        // 更新用户头像
        user.setAvatar(originUrl);
        this.updateById(user);

        return fileVO;
    }
}
