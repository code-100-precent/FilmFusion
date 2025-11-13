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
        String requestUri = request.getRequestURI();
        log.debug("AuthInterceptor拦截请求: {}", requestUri);
        
        // 先检查是否有PublicAccess注解，如果有则直接放行
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (handlerMethod.hasMethodAnnotation(PublicAccess.class)) {
                log.debug("接口 {} 标记为公开访问，直接放行", requestUri);
                return true;
            }
        }
        
        // 如果不是HandlerMethod（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            log.debug("非HandlerMethod请求，直接放行: {}", requestUri);
            return true;
        }
        
        try {
            // 从请求头中获取token
            String authHeader = request.getHeader("Authorization");
            log.debug("请求 {} 的Authorization头: {}", requestUri, authHeader != null ? "存在" : "不存在");
            
            if (authHeader == null || authHeader.trim().isEmpty()) {
                log.warn("请求 {} 缺少Authorization头", requestUri);
                throw new AuthorizationException("未登录或token已过期");
            }
            
            // 验证token
            String token = authHeader;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            log.debug("提取的token: {}", token.substring(0, Math.min(20, token.length())) + "...");
            
            if (!jwtConfig.validateToken(token)) {
                log.warn("请求 {} 的token验证失败", requestUri);
                throw new AuthorizationException("token无效");
            }
            log.debug("token验证通过");

            // 根据token类型设置对应的上下文
            String tokenType = getTokenType(token);
            log.debug("token类型: {}", tokenType);
            
            if ("user".equals(tokenType)) {
                cn.cxdproject.coder.model.entity.User user;
                try {
                    user = jwtConfig.getUserFromToken(token);
                    log.debug("从token解析用户: userId={}, username={}", user != null ? user.getId() : "null", user != null ? user.getUsername() : "null");
                } catch (Exception e) {
                    log.error("从token中解析用户信息失败", e);
                    throw new AuthorizationException("token无效");
                }
                if (user == null) {
                    log.error("从token中解析用户信息为null");
                    throw new AuthorizationException("token无效");
                }
                AuthContext.setCurrentUser(user);
                log.info("成功设置用户上下文: userId={}, username={}, URI={}", user.getId(), user.getUsername(), requestUri);
            } else {
                log.warn("token类型无效: {}, URI={}", tokenType, requestUri);
                throw new AuthorizationException("token类型无效");
            }

            AuthContext.setCurrentToken(token);
            log.debug("成功设置token到上下文");
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