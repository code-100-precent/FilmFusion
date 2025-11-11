package cn.cxdproject.coder.common.csrf;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * CSRF防护过滤器
 */
@Component
@Order(2)
public class CsrfFilter extends OncePerRequestFilter {

    private final CsrfTokenRepository tokenRepository;

    // 只包含安全的幂等方法
    private static final Set<String> SAFE_METHODS = new HashSet<>(
            Arrays.asList("GET", "HEAD", "TRACE", "OPTIONS", "POST", "DELETE", "PUT")
    );

    public CsrfFilter(CsrfTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String sessionId = request.getSession().getId();

        // 对非安全方法进行CSRF校验
        if (requiresCsrfProtection(request)) {
            String token = request.getHeader("X-CSRF-TOKEN");

            if (!tokenRepository.validateToken(sessionId, token)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF Token无效或缺失");
                return;
            }
        }

        // 如果Token不存在，则生成一个新的Token
        String existingToken = tokenRepository.getToken(sessionId);
        if (existingToken == null) {
            existingToken = tokenRepository.generateToken(sessionId);
        }

        // 始终将Token写回到响应头（便于前端保持同步）
        response.setHeader("X-CSRF-TOKEN", existingToken);

        filterChain.doFilter(request, response);
    }

    /**
     * 判断请求是否需要CSRF保护
     */
    private boolean requiresCsrfProtection(HttpServletRequest request) {
        return !SAFE_METHODS.contains(request.getMethod());
    }
}
