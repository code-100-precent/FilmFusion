package cn.cxdproject.coder.common.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static cn.cxdproject.coder.common.constants.Constants.STORAGE_TYPE_LOCAL;
import static cn.cxdproject.coder.common.constants.Constants.STORAGE_TYPE_MINIO;

/**
 * 存储服务配置
 */
@Configuration
public class StorageConfig {

    /**
     * Minio存储服务
     */
    private final MinioStorageService minioStorageService;

    /**
     * 本地存储服务
     */
    private final LocalStorageService localStorageService;

    /**
     * 文件存储属性
     */
    private final FileStorageProperties properties;

    public StorageConfig(MinioStorageService minioStorageService, LocalStorageService localStorageService, FileStorageProperties properties) {
        this.minioStorageService = minioStorageService;
        this.localStorageService = localStorageService;
        this.properties = properties;
    }

    /**
     * 获取文件存储服务
     */
    @Bean
    public FileStorageAdapter fileStorageAdapter() {
        return switch (properties.getType()) {
            case STORAGE_TYPE_MINIO -> minioStorageService;
            case STORAGE_TYPE_LOCAL -> localStorageService;
            default -> throw new IllegalArgumentException("不支持的存储类型: " + properties.getType());
        };
    }
}