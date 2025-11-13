package cn.cxdproject.coder.interceptor;

import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.config.JwtConfig;
import cn.cxdproject.coder.exception.AuthorizationException;
import cn.cxdproject.coder.model.entity.Admin;
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
            Admin adminFromToken = jwtConfig.getAdminFromToken(token);
            if (!adminFromToken.getRole().equals(ADMIN)){
                throw new AuthorizationException("insufficient authority");
            }

            AuthContext.setCurrentAdmin(adminFromToken);
            AuthContext.setCurrentToken(token);
            return true;
        } catch (AuthorizationException e) {
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }
            // 如果方法上有PublicAccess注解，则允许公开访问
            if (((HandlerMethod) handler).hasMethodAnnotation(PublicAccess.class)) {
                return true;
            }
            // 返回401未授权错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
            return false;
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            AuthContext.clear();
        } catch (Exception e) {
            log.warn("清理AuthContext失败", e);
        }
    }
}
