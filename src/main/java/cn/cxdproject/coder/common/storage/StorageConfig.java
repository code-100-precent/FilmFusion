package cn.cxdproject.coder.common.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.cxdproject.coder.common.constants.Constants.STORAGE_TYPE_LOCAL;



/**
 * 存储服务配置
 */
@Configuration
public class StorageConfig {

    /**
     * 本地存储服务
     */
    private final LocalStorageService localStorageService;

    /**
     * 文件存储属性
     */
    private final FileStorageProperties properties;

    public StorageConfig(LocalStorageService localStorageService, FileStorageProperties properties) {
        this.localStorageService = localStorageService;
        this.properties = properties;
    }

    /**
     * 获取文件存储服务
     */
    @Bean
    public FileStorageAdapter fileStorageAdapter() {
        return switch (properties.getType()) {
            case STORAGE_TYPE_LOCAL -> localStorageService;
            default -> throw new IllegalArgumentException("不支持的存储类型: " + properties.getType());
        };
    }
}