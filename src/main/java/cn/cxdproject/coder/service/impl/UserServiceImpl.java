package cn.cxdproject.coder.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.mapper.UserMapper;
import cn.cxdproject.coder.service.UserService;
import org.springframework.stereotype.Service;

/**
 * User 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
