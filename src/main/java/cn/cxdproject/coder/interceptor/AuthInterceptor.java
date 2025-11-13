package cn.cxdproject.coder.interceptor;

import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.config.JwtConfig;
import cn.cxdproject.coder.exception.AuthorizationException;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 认证拦截器（支持Admin和Visitor）
 *
 * @author heathcetide
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtConfig jwtConfig;

    public AuthInterceptor(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    /**
     * 获取Token类型（admin或visitor）
     */
    private String getTokenType(String token) {
        try {
            Claims claims = jwtConfig.getClaimsFromToken(token);
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        // 先检查是否有PublicAccess注解，如果有则直接放行
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(PublicAccess.class)) {
                log.debug("接口 {} 标记为公开访问，直接放行", request.getRequestURI());
                return true;
            }
        }
        
        // 如果不是HandlerMethod（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        
        try {
            // 从请求头中获取token
            String token = request.getHeader("Authorization");
            if (token == null || token.trim().isEmpty()) {
                throw new AuthorizationException("未登录或token已过期");
            }
            // 验证token
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            if (!jwtConfig.validateToken(token)) {
                throw new AuthorizationException("token无效");
            }

            // 根据token类型设置对应的上下文
            String tokenType = getTokenType(token);
            if ("user".equals(tokenType)) {
                AuthContext.setCurrentUser(jwtConfig.getUserFromToken(token));
            } else {
                throw new AuthorizationException("token类型无效");
            }

            AuthContext.setCurrentToken(token);
            return true;
        } catch (AuthorizationException e) {
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }
            // 返回401未授权错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
            return false;
        } catch (Exception e) {
            // 返回500服务器内部错误
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