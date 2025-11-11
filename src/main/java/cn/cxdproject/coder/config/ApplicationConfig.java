package cn.cxdproject.coder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 程序注解配置
 *
 * @author heathcetide
 */
@Data
@Component
@ConfigurationProperties(prefix = "code100")
public class ApplicationConfig {

    /**
     * 项目名称，如 "code100"
     */
    private String name;
    /**
     * 项目版本号，由 Maven 属性注入
     */
    private String version;
    /**
     * 构建时间，例如 "2024-03-19 12:00:00"
     */
    private String buildTime;
    /**
     * 版权信息
     */
    private Copyright copyright;

    /**
     * 内部类用于封装版权相关的配置信息
     */
    @Data
    public static class Copyright {
        /**
         * 版权所有者，如 "code100 Team"
         */
        private String owner;
        /**
         * 版权起始年份，如 2024
         */
        private int sinceYear;
        /**
         * 许可证类型，如 "MIT"
         */
        private String license;
    }
}
