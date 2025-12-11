package cn.cxdproject.coder.common.storage;

import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.model.vo.FileVO;
import io.minio.*;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.concurrent.CompletableFuture;

import static cn.cxdproject.coder.common.constants.Constants.HTTP;
import static cn.cxdproject.coder.common.constants.Constants.HTTPS;
import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.SYSTEM_ERROR;


/**
 * MinIO存储服务
 */
@Service
public class MinioStorageService implements FileStorageAdapter {

    /**
     * MinIO客户端
     */
    private final MinioClient minioClient;

    /**
     * 文件存储属性
     */
    private final FileStorageProperties properties;

    public MinioStorageService(MinioClient minioClient, FileStorageProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    /**
     * 上传文件
     */
    @Override
    public FileVO upload(MultipartFile file) {
        try {
            String baseName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String originalFilename = "origin/" + baseName;
            String thumbFilename = "thumb/" + baseName;
            String originalUrl = null;
            String thumbUrl = null;

            byte[] fileBytes = file.getBytes();
            String contentType = file.getContentType();

            // 1. 上传原图
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(originalFilename)
                            .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                            .contentType(contentType)
                            .build()
            );
            originalUrl = properties.getMinioEndpoint() + '/' + properties.getMinioBucket() + '/' + originalFilename;
            // 2. 如果是图片，异步生成缩略图
            if (isImageFile(file)) {
                CompletableFuture.runAsync(() -> {
                    try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                        generateAndUploadThumbnail(thumbFilename, is);
                    } catch (Exception e) {
                        throw new RuntimeException("压缩图片失败", e);
                    }
                });
                thumbUrl = properties.getMinioEndpoint() + '/' + properties.getMinioBucket() + '/' + thumbFilename;
            }

            return new FileVO(originalUrl,thumbUrl);
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR.code(), "MinIO上传失败: " + e.getMessage());
        }
    }

    @Override
    public FileVO upload(String prefix, MultipartFile file) {
        try {
            String baseName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String originalFilename = "origin/" + baseName;
            String thumbFilename = "thumb/" + baseName;
            String originalUrl;
            String thumbUrl;

            byte[] fileBytes = file.getBytes();
            String contentType = file.getContentType();

            // 1. 上传原图
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(originalFilename)
                            .stream(new ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                            .contentType(contentType)
                            .build()
            );

            // 2. 如果是图片，异步生成缩略图
            if (isImageFile(file)) {
                CompletableFuture.runAsync(() -> {
                    try (InputStream is = new ByteArrayInputStream(fileBytes)) {
                        generateAndUploadThumbnail(thumbFilename, is);
                    } catch (Exception e) {
                        throw new RuntimeException("压缩图片失败", e);
                    }
                });
            }

            originalUrl = properties.getMinioEndpoint() + '/' + properties.getMinioBucket() + '/' + originalFilename;
            thumbUrl = properties.getMinioEndpoint() + '/' + properties.getMinioBucket() + '/' + thumbFilename;


            return new FileVO(originalUrl,thumbUrl);
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR.code(), "MinIO上传失败" + e);
        }
    }

    /**
     * 下载文件
     */
    @Override
    public byte[] download(String path) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(properties.getMinioBucket())
                        .object(extractFilenameFromPath(path))
                        .build())) {
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("MinIO下载失败", e);
        }
    }

    /**
     * 删除文件
     */
    @Override
    public void delete(String path) {
        try {
            String filename = extractFilenameFromPath(path);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(filename)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO删除失败", e);
        }
    }

    /**
     * 文件是否存在
     */
    @Override
    public boolean exists(String path) {
        try {
            String filename = extractFilenameFromPath(path);
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(filename)
                            .build());
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 获取文件URL
     */
    @Override
    public String getUrl(String path) {
        return properties.getMinioEndpoint() + "/" + properties.getMinioBucket() + "/" + path;
    }

    /**
     * 获取文件大小
     */
    @Override
    public long getFileSize(String path) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(extractFilenameFromPath(path))
                            .build()).size();
        } catch (Exception e) {
            throw new RuntimeException("获取MinIO文件大小失败", e);
        }
    }

    /**
     * 获取文件类型
     */
    @Override
    public String getContentType(String path) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(extractFilenameFromPath(path))
                            .build()).contentType();
        } catch (Exception e) {
            throw new RuntimeException("获取MinIO文件类型失败", e);
        }
    }

    /**
     * 重命名文件
     */
    @Override
    public void rename(String oldPath, String newPath) {
        copy(oldPath, newPath);
        delete(oldPath);
    }

    /**
     * 移动文件
     */
    @Override
    public void copy(String sourcePath, String targetPath) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .source(CopySource.builder()
                                    .bucket(properties.getMinioBucket())
                                    .object(sourcePath)
                                    .build())
                            .bucket(properties.getMinioBucket())
                            .object(targetPath)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("MinIO复制失败", e);
        }
    }

    /**
     * 移动文件
     */
    @Override
    public void move(String sourcePath, String targetPath) {
        rename(sourcePath, targetPath);
    }

    /**
     * 从完整路径中提取文件名
     */
    private String extractFilenameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }

        // 如果是完整URL，提取文件名部分
        if (path.startsWith(HTTP) || path.startsWith(HTTPS)) {
            int lastSlashIndex = path.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < path.length() - 1) {
                return path.substring(lastSlashIndex + 1);
            }
        }
        return path;
    }

    /**
     * 上传文件
     */
    @Override
    public String uploadFile(File file, String filename) {
        try {
            InputStream inputStream = new FileInputStream(file);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(filename)
                            .stream(inputStream, file.length(), -1)
                            .contentType("application/octet-stream") // Set appropriate content type
                            .build()
            );
            return properties.getMinioEndpoint() + '/' + properties.getMinioBucket() + '/' + filename;
        } catch (Exception e) {
            throw new BusinessException(SYSTEM_ERROR.code(), "MinIO上传失败: " + e);
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType == null || originalFilename == null) {
            return false;
        }

        boolean validContentType = contentType.startsWith("image/");
        boolean validExtension = originalFilename.matches(".*\\.(?i)(jpg|jpeg|png|gif|webp)$");

        return validContentType && validExtension;
    }

    // 生成缩略图并上传到 MinIO
    private void generateAndUploadThumbnail(String thumbKey, InputStream originalStream) throws Exception {
        try (ByteArrayOutputStream thumbOut = new ByteArrayOutputStream()) {
            Thumbnails.of(originalStream)
                    .size(300, 300)           // 最大宽高 300px（等比缩放）
                    .outputQuality(0.4f)      // JPEG 质量 40%
                    .outputFormat("jpg")      // 统一转为 jpg（减小体积）
                    .toOutputStream(thumbOut);

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getMinioBucket())
                            .object(thumbKey)
                            .stream(new ByteArrayInputStream(thumbOut.toByteArray()), thumbOut.size(), -1)
                            .contentType("image/jpeg")
                            .build()
            );
        }
    }
}
