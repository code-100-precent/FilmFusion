package cn.cxdproject.coder.common.storage;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 客户端配置类
 * MinioClient 是官方提供的 Java SDK 客户端，用于执行对象存储的各种操作（如上传、下载、删除等）。
 *
 * @author heathcetide
 */
@Configuration
public class MinioClientConfig {

    /**
     * Minio客户端
     */
    @Bean
    public MinioClient minioClient(FileStorageProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getMinioEndpoint())
                .credentials(properties.getMinioAccessKey(), properties.getMinioSecretKey())
                .build();
    }
}