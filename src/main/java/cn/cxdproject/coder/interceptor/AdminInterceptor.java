package cn.cxdproject.coder.interceptor;

import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.config.JwtConfig;
import cn.cxdproject.coder.exception.AuthorizationException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static cn.cxdproject.coder.common.constants.RoleConstants.ADMIN;


/**
 * 管理员拦截器
 *
 * @author heathcetide
 */
@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    private final JwtConfig jwtConfig;

    public AdminInterceptor(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        // 先检查是否有PublicAccess注解，如果有则直接放行
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(PublicAccess.class)) {
                return true;
            }
        }

        try {
            // 从请求头中获取token
            String token = request.getHeader("Authorization");
            if (token == null) {
                throw new AuthorizationException("未登录或token已过期");
            }
            
            // 验证token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            
            if (!jwtConfig.validateToken(token)) {
                throw new AuthorizationException("token无效");
            }
            
            // 从token获取用户信息
            cn.cxdproject.coder.model.entity.User user = jwtConfig.getUserFromToken(token);
            if (user == null) {
                throw new AuthorizationException("用户信息无效");
            }
            
            // 检查是否是管理员
            String role = user.getRole();
            if (role == null || (!ADMIN.equals(role) && !"SUPER_ADMIN".equals(role))) {
                throw new AuthorizationException("权限不足，需要管理员权限");
            }
            
            // 检查用户是否被禁用或删除
            if (Boolean.FALSE.equals(user.getEnabled())) {
                throw new AuthorizationException("用户已被禁用");
            }
            
            // 设置上下文（AuthInterceptor已经设置过了，这里确保设置正确）
            AuthContext.setCurrentUser(user);
            AuthContext.setCurrentToken(token);
            
            return true;
        } catch (AuthorizationException e) {
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }
            // 返回403权限不足错误
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":403,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
            return false;
        } catch (Exception e) {
            log.error("管理员拦截器处理异常", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"系统错误\",\"data\":null}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // AuthContext的清理由AuthInterceptor负责，这里不需要重复清理
    }
}
