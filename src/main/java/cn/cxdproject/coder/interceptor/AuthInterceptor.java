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

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        // 公开接口直接放行
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(PublicAccess.class)) {
                return true;
            }
        }

        // 如果不是 HandlerMethod（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String requestUri = request.getRequestURI();
        try {
            // 从请求头中获取 token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || authHeader.trim().isEmpty()) {
                throw new AuthorizationException("未登录或token已过期");
            }

            // 提取 token
            String token = authHeader;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // 一次解析完成签名验证 + 黑名单 + 用户级失效检查，
            // 后续 getTokenType / 构造 User 都复用同一份 Claims，避免重复签名校验。
            Claims claims = jwtConfig.parseAndValidate(token);
            if (claims == null) {
                throw new AuthorizationException("token无效");
            }

            String tokenType = jwtConfig.getTokenType(claims);
            if (!"user".equals(tokenType)) {
                log.warn("token类型无效: {}, URI={}", tokenType, requestUri);
                throw new AuthorizationException("token类型无效");
            }

            cn.cxdproject.coder.model.entity.User user;
            try {
                user = jwtConfig.getUserFromClaims(claims);
            } catch (Exception e) {
                log.warn("从token中解析用户信息失败: {}", e.getMessage());
                throw new AuthorizationException("token无效");
            }
            if (user == null) {
                throw new AuthorizationException("token无效");
            }
            AuthContext.setCurrentUser(user);
            AuthContext.setCurrentToken(token);
            // 不在每个请求都打 info 日志，避免日志量爆炸 + username 泄露到 info 级别
            if (log.isDebugEnabled()) {
                log.debug("auth ok userId={}, URI={}", user.getId(), requestUri);
            }
            return true;
        } catch (AuthorizationException e) {
            // 返回401未授权错误
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"" + e.getMessage() + "\",\"data\":null}");
            return false;
        } catch (io.jsonwebtoken.JwtException e) {
            // JWT相关异常也返回401
            log.warn("JWT异常: {}", e.getMessage());
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"token无效或已过期\",\"data\":null}");
            return false;
        } catch (Exception e) {
            // 返回500服务器内部错误
            log.error("拦截器处理异常: {}", e.getMessage(), e);
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }
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