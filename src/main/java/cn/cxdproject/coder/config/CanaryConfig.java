package cn.cxdproject.coder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.web.servlet.function.RouterFunctions.route;

/**
 * 灰度发布配置类
 *
 * @author heathcetide
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "code100.feature.canary", havingValue = "true")
public class CanaryConfig {

    @Value("${code100.feature.canary-header:X-User-ID}")
    private String canaryHeader;

    @Value("${code100.feature.canary-rate:10}")
    private int canaryRate;

    @Bean
    public RouterFunction<ServerResponse> canaryRouter() {
        return route()
                .GET("/api/**", this::handleCanary)
                .POST("/api/**", this::handleCanary)
                .PUT("/api/**", this::handleCanary)
                .DELETE("/api/**", this::handleCanary)
                .build();
    }

    private ServerResponse handleCanary(ServerRequest request) {
        String userId = request.headers().firstHeader(canaryHeader);
        String path = request.path();

        if (userId != null && isCanaryUser(userId)) {
            log.info("用户 [{}] 命中灰度策略，重定向到新版路径 /api/v2{}", userId, path);
            return ServerResponse.status(HttpStatus.PERMANENT_REDIRECT)
                    .location(URI.create("/api/v2" + path))
                    .build();
        }

        return ServerResponse.ok().build();
    }

    private boolean isCanaryUser(String userId) {
        int hash = Math.abs(userId.hashCode() % 100);
        return hash < canaryRate;
    }
}