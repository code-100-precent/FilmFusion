package cn.cxdproject.coder.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global CORS (Cross-Origin Resource Sharing) configuration class
 * Used to resolve cross-origin request issues for Spring Boot backend APIs.
 *
 * @author heathcetide
 */
@Configuration
public class GlobalCorsConfig {

    @Value("${code100.security.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://127.0.0.1:5173,http://127.0.0.1:3000}")
    private String allowedOrigins;

    @Value("${code100.security.cors.allowed-origin-patterns:}")
    private String allowedOriginPatterns;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        List<String> originList = splitConfig(allowedOrigins);
        if (!originList.isEmpty()) {
            config.setAllowedOrigins(originList);
        }

        List<String> originPatternList = splitConfig(allowedOriginPatterns);
        if (!originPatternList.isEmpty()) {
            config.setAllowedOriginPatterns(originPatternList);
        }

        config.setAllowCredentials(true);

        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "X-Auth-Token",
                "X-Trace-Id"
        ));
        config.setExposedHeaders(Arrays.asList("Authorization", "X-Auth-Token", "X-Trace-Id"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    private List<String> splitConfig(String value) {
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !"*".equals(s))
                .collect(Collectors.toList());
    }
}
