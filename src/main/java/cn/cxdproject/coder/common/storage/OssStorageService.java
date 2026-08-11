package cn.cxdproject.coder.common.storage;

import cn.cxdproject.coder.exception.SystemException;
import cn.cxdproject.coder.model.vo.FileVO;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.SYSTEM_ERROR;

@Slf4j
@Component
@ConditionalOnProperty(name = "code100.storage.type", havingValue = "cos")
public class OssStorageService implements FileStorageAdapter {
    private final FileStorageProperties properties;
    private final COSClient cosClient;

    public OssStorageService(FileStorageProperties properties) {
        this.properties = properties;
        COSCredentials credentials = new BasicCOSCredentials(properties.getCosSecretId(), properties.getCosSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(properties.getCosRegion()));
        this.cosClient = new COSClient(credentials, clientConfig);
    }

    @Override
    public FileVO upload(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("上传的文件不能为空");
            }
            String baseName = buildBaseName(file);
            String originKey = "origin/" + baseName;
            String thumbKey = "thumb/" + baseName;

            uploadImage(file, originKey);

            String originalUrl = getUrl(originKey);
            String thumbnailUrl = null;

            if (isImageFile(file) && !isWebp(file)) {
                uploadThumbnail(file, thumbKey);
                thumbnailUrl = getUrl(thumbKey);
            }

            return new FileVO(originalUrl, thumbnailUrl);
        } catch (IOException | CosClientException e) {
            log.error("COS文件上传失败 - 文件名: {}, 错误: {}", file != null ? file.getOriginalFilename() : null, e.getMessage(), e);
            throw new SystemException(SYSTEM_ERROR.code(), "COS文件上传失败", e);
        }
    }

    @Override
    public FileVO upload(String prefix, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("上传的文件不能为空");
            }
            String baseName = buildBaseName(file);
            String normalizedPrefix = trimStartSlash(prefix);
            String originKey = normalizedPrefix + "/origin/" + baseName;
            String thumbKey = normalizedPrefix + "/thumb/" + baseName;

            uploadImage(file, originKey);

            String originalUrl = getUrl(originKey);
            String thumbnailUrl = null;

            if (isImageFile(file) && !isWebp(file)) {
                uploadThumbnail(file, thumbKey);
                thumbnailUrl = getUrl(thumbKey);
            }

            return new FileVO(originalUrl, thumbnailUrl);
        } catch (IOException | CosClientException e) {
            log.error("COS文件上传失败 - prefix: {}, 文件名: {}, 错误: {}", prefix, file != null ? file.getOriginalFilename() : null, e.getMessage(), e);
            throw new SystemException(SYSTEM_ERROR.code(), "COS文件上传失败", e);
        }
    }

    public String uploadImage(MultipartFile file, String key) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }

        ObjectMetadata metadata = new ObjectMetadata();
        String contentType = file.getContentType();
        if (contentType == null || contentType.isEmpty() || "application/octet-stream".equalsIgnoreCase(contentType)) {
            if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
                contentType = "image/jpeg";
            } else if (".png".equals(ext)) {
                contentType = "image/png";
            } else if (".gif".equals(ext)) {
                contentType = "image/gif";
            } else if (".webp".equals(ext)) {
                contentType = "image/webp";
            } else if (".bmp".equals(ext)) {
                contentType = "image/bmp";
            }
        }
        if (contentType != null && !contentType.isEmpty()) {
            metadata.setContentType(contentType);
        }
        metadata.setContentLength(file.getSize());

        PutObjectRequest request = new PutObjectRequest(properties.getCosBucket(), key, file.getInputStream(), metadata);
        cosClient.putObject(request);

        return getUrl(key);
    }

    @Override
    public byte[] download(String path) {
        try (COSObject cosObject = cosClient.getObject(properties.getCosBucket(), extractKey(path))) {
            return cosObject.getObjectContent().readAllBytes();
        } catch (IOException e) {
            throw new SystemException(SYSTEM_ERROR.code(), "COS文件下载失败", e);
        }
    }

    @Override
    public void delete(String path) {
        cosClient.deleteObject(properties.getCosBucket(), extractKey(path));
    }

    @Override
    public boolean exists(String path) {
        return cosClient.doesObjectExist(properties.getCosBucket(), extractKey(path));
    }

    @Override
    public String getUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String key = extractKey(path);
        if (properties.getCosUrlPrefix() != null && !properties.getCosUrlPrefix().isEmpty()) {
            return trimEndSlash(properties.getCosUrlPrefix()) + "/" + key;
        }
        return String.format("https://%s.cos.%s.myqcloud.com/%s", properties.getCosBucket(), properties.getCosRegion(), key);
    }

    @Override
    public long getFileSize(String path) {
        return cosClient.getObjectMetadata(properties.getCosBucket(), extractKey(path)).getContentLength();
    }

    @Override
    public String getContentType(String path) {
        return cosClient.getObjectMetadata(properties.getCosBucket(), extractKey(path)).getContentType();
    }

    @Override
    public void rename(String oldPath, String newPath) {
        copy(oldPath, newPath);
        delete(oldPath);
    }

    @Override
    public void copy(String sourcePath, String targetPath) {
        cosClient.copyObject(properties.getCosBucket(), extractKey(sourcePath), properties.getCosBucket(), extractKey(targetPath));
    }

    @Override
    public void move(String sourcePath, String targetPath) {
        rename(sourcePath, targetPath);
    }

    @Override
    public String uploadFile(File file, String filename) {
        String key = trimStartSlash(filename);
        cosClient.putObject(properties.getCosBucket(), key, file);
        return getUrl(key);
    }

    @PreDestroy
    public void destroy() {
        cosClient.shutdown();
    }

    private String extractKey(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        String key = path;
        String publicPrefix = String.format("https://%s.cos.%s.myqcloud.com/", properties.getCosBucket(), properties.getCosRegion());
        if (key.startsWith(publicPrefix)) {
            key = key.substring(publicPrefix.length());
        } else if (properties.getCosUrlPrefix() != null && !properties.getCosUrlPrefix().isEmpty()) {
            String configuredPrefix = trimEndSlash(properties.getCosUrlPrefix()) + "/";
            if (key.startsWith(configuredPrefix)) {
                key = key.substring(configuredPrefix.length());
            }
        }
        if (key.startsWith("/files/")) {
            key = key.substring(7);
        } else if (key.startsWith("files/")) {
            key = key.substring(6);
        }
        return trimStartSlash(key);
    }

    private String buildBaseName(MultipartFile file) {
        String safeFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("[/\\\\]", "_")
                : "file";
        return System.currentTimeMillis() + "_" + safeFilename;
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

    private boolean isWebp(MultipartFile file) {
        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        return "image/webp".equalsIgnoreCase(contentType)
                || (originalFilename != null && originalFilename.toLowerCase().endsWith(".webp"));
    }

    private void uploadThumbnail(MultipartFile file, String key) throws IOException {
        Path tempFile = Files.createTempFile("cos-thumb-", ".jpg");
        try {
            Thumbnails.of(file.getInputStream())
                    .size(200, 200)
                    .keepAspectRatio(true)
                    .outputQuality(0.8f)
                    .outputFormat("jpg")
                    .toFile(tempFile.toFile());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(Files.size(tempFile));
            metadata.setContentType("image/jpeg");
            try (InputStream inputStream = Files.newInputStream(tempFile)) {
                PutObjectRequest request = new PutObjectRequest(properties.getCosBucket(), key, inputStream, metadata);
                cosClient.putObject(request);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    private String trimStartSlash(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replaceAll("^/+", "");
    }

    private String trimEndSlash(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.replaceAll("/+$", "");
    }
}
