package cn.cxdproject.coder.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件存储配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "code100.storage")
public class FileStorageProperties {

    /**
     * 存储类型：local 或 minio
     */
    private String type;

    // MinIO

    /**
     * MinIO 端点
     */
    private String minioEndpoint;

    /**
     * MinIO 访问密钥
     */
    private String minioAccessKey;

    /**
     * MinIO 密钥
     */
    private String minioSecretKey;

    /**
     * MinIO 存储桶
     */
    private String minioBucket;

    // Local

    /**
     * 本地存储路径
     */
    private String localBasePath;

    // COS 配置

    /**
     * COS 密钥
     */
    private String cosSecretId;

    /**
     * COS 密钥
     */
    private String cosSecretKey;

    /**
     * COS 区域
     */
    private String cosRegion;

    /**
     * COS 存储桶
     */
    private String cosBucket;

    /**
     * COS 访问地址前缀
     */
    private String cosUrlPrefix;
}