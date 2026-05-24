package cn.cxdproject.coder.common.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class SwaggerAccessFilter extends OncePerRequestFilter {
    private static final List<String> SWAGGER_PATHS = Arrays.asList(
            "/doc.html",
            "/swagger-ui.html",
            "/swagger-ui/",
            "/swagger-resources",
            "/v2/api-docs",
            "/v3/api-docs",
            "/webjars/"
    );

    @Value("${code100.swagger.enabled:false}")
    private boolean swaggerEnabled;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!swaggerEnabled && isSwaggerPath(request.getRequestURI(), request.getContextPath())) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isSwaggerPath(String requestUri, String contextPath) {
        String path = requestUri;
        if (contextPath != null && !contextPath.isEmpty() && path.startsWith(contextPath)) {
            path = path.substring(contextPath.length());
        }
        for (String swaggerPath : SWAGGER_PATHS) {
            if (path.equals(swaggerPath) || path.startsWith(swaggerPath)) {
                return true;
            }
        }
        return false;
    }
}
