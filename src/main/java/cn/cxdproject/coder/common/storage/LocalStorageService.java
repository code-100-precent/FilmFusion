package cn.cxdproject.coder.common.storage;


import cn.cxdproject.coder.model.vo.FileVO;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储服务
 */
@Service
public class LocalStorageService implements FileStorageAdapter {

    /**
     * 存储属性类
     */
    private final FileStorageProperties properties;

    public LocalStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }
    /**
     * 上传文件
     */
    @Override
    public FileVO upload(MultipartFile file) {
        try {
            String baseName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            // 构造本地存储路径
            Path originDir = Paths.get(properties.getLocalBasePath(), "origin");
            Path thumbDir = Paths.get(properties.getLocalBasePath(), "thumb");
            Files.createDirectories(originDir);
            Files.createDirectories(thumbDir);

            Path originPath = originDir.resolve(baseName);
            Path thumbPath = thumbDir.resolve(baseName);

            // 1. 保存原图
            file.transferTo(originPath.toFile());

            String baseUrl = "/api/files"; // 假设你通过此路径提供静态资源访问
            String originalUrl = baseUrl + "/origin/" + baseName;
            String thumbnailUrl = null;

            // 2. 如果是图片，生成缩略图（建议同步生成，避免前端访问不到）
            if (isImageFile(file)) {
                thumbnailUrl = baseUrl + "/thumb/" + baseName;
                // 同步生成缩略图（更可靠）
                generateLocalThumbnail(originPath, thumbPath);
            }

            return new FileVO(originalUrl, thumbnailUrl);
        } catch (IOException e) {
            throw new RuntimeException("本地文件上传失败", e);
        }
    }

    @Override
    public FileVO upload(String prefix, MultipartFile file) {

        try {
            String baseName = System.currentTimeMillis() + "_" + prefix + "/" + file.getOriginalFilename();

            // 构造本地存储路径
            Path originDir = Paths.get(properties.getLocalBasePath(), "origin");
            Path thumbDir = Paths.get(properties.getLocalBasePath(), "thumb");
            Files.createDirectories(originDir);
            Files.createDirectories(thumbDir);

            Path originPath = originDir.resolve(baseName);
            Path thumbPath = thumbDir.resolve(baseName);

            // 1. 保存原图
            file.transferTo(originPath.toFile());

            String baseUrl = "/api/files"; // 假设你通过此路径提供静态资源访问
            String originalUrl = baseUrl + "/origin/" + baseName;
            String thumbnailUrl = null;

            // 2. 如果是图片，生成缩略图（建议同步生成，避免前端访问不到）
            if (isImageFile(file)) {
                thumbnailUrl = baseUrl + "/thumb/" + baseName;
                // 同步生成缩略图（更可靠）
                generateLocalThumbnail(originPath, thumbPath);
            }

            return new FileVO(originalUrl, thumbnailUrl);
        } catch (IOException e) {
            throw new RuntimeException("本地文件上传失败", e);
        }
    }

    /**
     * 下载文件
     */
    @Override
    public byte[] download(String path) {
        try {
            Path filePath = Paths.get(properties.getLocalBasePath(), path);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("本地文件下载失败", e);
        }
    }

    /**
     * 删除文件
     */
    @Override
    public void delete(String path) {
        try {
            Path filePath = Paths.get(properties.getLocalBasePath(), path);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("本地文件删除失败", e);
        }
    }


    /**
     * 文件是否存在
     */
    @Override
    public boolean exists(String path) {
        return Files.exists(Paths.get(properties.getLocalBasePath(), path));
    }

    /**
     * 获取文件URL
     */
    @Override
    public String getUrl(String path) {
        return "file://" + Paths.get(properties.getLocalBasePath(), path).toAbsolutePath();
    }

    /**
     * 获取文件大小
     */
    @Override
    public long getFileSize(String path) {
        try {
            return Files.size(Paths.get(properties.getLocalBasePath(), path));
        } catch (IOException e) {
            throw new RuntimeException("获取文件大小失败", e);
        }
    }

    /**
     * 获取文件类型
     */
    @Override
    public String getContentType(String path) {
        try {
            return Files.probeContentType(Paths.get(properties.getLocalBasePath(), path));
        } catch (IOException e) {
            throw new RuntimeException("获取文件类型失败", e);
        }
    }

    /**
     * 文件重命名
     */
    @Override
    public void rename(String oldPath, String newPath) {
        try {
            Path source = Paths.get(properties.getLocalBasePath(), oldPath);
            Path target = Paths.get(properties.getLocalBasePath(), newPath);
            Files.move(source, target);
        } catch (IOException e) {
            throw new RuntimeException("文件重命名失败", e);
        }
    }

    /**
     * 文件复制
     */
    @Override
    public void copy(String sourcePath, String targetPath) {
        try {
            Files.copy(
                    Paths.get(properties.getLocalBasePath(), sourcePath),
                    Paths.get(properties.getLocalBasePath(), targetPath),
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException("文件复制失败", e);
        }
    }

    /**
     * 移动 文件
     */
    @Override
    public void move(String sourcePath, String targetPath) {
        rename(sourcePath, targetPath);
    }

    /**
     * 上传文件
     */
    @Override
    public String uploadFile(File file, String filename) {
        try {
            // Prepare the path where the file will be stored
            Path path = Paths.get(properties.getLocalBasePath(), filename);
            Files.createDirectories(path.getParent());
            // Copy the provided file to the destination path
            Files.copy(file.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("本地文件上传失败", e);
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

    // 生成缩略图并上传到本地
    private void generateLocalThumbnail(Path originPath, Path thumbPath) throws IOException {
        Thumbnails.of(originPath.toFile())
                .size(200, 200)          // 缩略图尺寸
                .keepAspectRatio(true)
                .outputQuality(0.8f)
                .toFile(thumbPath.toFile());
    }
}
