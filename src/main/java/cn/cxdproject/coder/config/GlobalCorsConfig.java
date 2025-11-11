package cn.cxdproject.coder.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Global CORS (Cross-Origin Resource Sharing) configuration class
 * Used to resolve cross-origin request issues for Spring Boot backend APIs.
 *
 * @author heathcetide
 */
@Configuration
public class GlobalCorsConfig {

    /**
     * Creates a global CORS filter Bean.
     * Spring Boot will automatically register this filter to the container to handle cross-origin requests before each request.
     *
     * @return CorsFilter cross-origin filter
     */
    @Bean
    public CorsFilter corsFilter() {
        // Create a CORS configuration object
        CorsConfiguration config = new CorsConfiguration();

        // Set allowed frontend domain (supports wildcard, such as http://localhost:3000)
        // Here we allow all domains to access (not recommended for production)
        config.addAllowedOriginPattern("*");

        // Whether to allow sending cookies (e.g., session, token)
        config.setAllowCredentials(true);

        // Set allowed request headers (e.g., Authorization, custom Token headers, etc.)
        config.addAllowedHeader("*"); // Allow all request headers

        // Set allowed HTTP request methods
        config.addAllowedMethod("*"); // Allow all HTTP methods: GET, POST, PUT, DELETE, etc.

        // Create URL mapping source object
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // Apply the above CORS configuration to all request paths (/**)
        source.registerCorsConfiguration("/**", config);

        // Create and return a CorsFilter instance, applying global cross-origin configuration
        return new CorsFilter(source);
    }
}
