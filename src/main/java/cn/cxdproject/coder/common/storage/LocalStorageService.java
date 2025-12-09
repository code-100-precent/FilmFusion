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

            // ✅ 关键改进：解析为绝对路径，并标准化（防止 ../ 等）
            Path basePath = Paths.get(properties.getLocalBasePath()).toAbsolutePath().normalize();
            Path originDir = basePath.resolve("origin");
            Path thumbDir = basePath.resolve("thumb");

            // 创建目录（幂等操作，存在也不报错）
            Files.createDirectories(originDir);
            Files.createDirectories(thumbDir);

            Path originPath = originDir.resolve(baseName);
            Path thumbPath = thumbDir.resolve(baseName);

            // 保存原图
            file.transferTo(originPath.toFile());

            String originalUrl = "/files/origin/" + baseName;
            String thumbnailUrl = null;

            if (isImageFile(file)) {
                thumbnailUrl = "/files/thumb/" + baseName;
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

            // 返回相对路径，前端会根据配置拼接完整URL
            String originalUrl = "/files/" + prefix + "/origin/" + baseName;
            String thumbnailUrl = null;

            // 2. 如果是图片，生成缩略图（建议同步生成，避免前端访问不到）
            if (isImageFile(file)) {
                thumbnailUrl = "/files/" + prefix + "/thumb/" + baseName;
                // 同步生成缩略图（更可靠）
                generateLocalThumbnail(originPath, thumbPath);
            }

            return new FileVO(originalUrl, thumbnailUrl);
        } catch (IOException e) {
            throw new RuntimeException("本地文件上传失败", e);
        }
    }

    /**
     * 将URL路径转换为本地文件系统路径
     * 例如: /files/avatars/origin/xxx.jpg -> avatars/origin/xxx.jpg
     */
    private String convertUrlPathToLocalPath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        // 移除 /files 前缀（如果存在）
        if (path.startsWith("/files/")) {
            path = path.substring(7); // 移除 "/files/"
        } else if (path.startsWith("files/")) {
            path = path.substring(6); // 移除 "files/"
        }
        return path;
    }

    /**
     * 下载文件
     */
    @Override
    public byte[] download(String path) {
        try {
            String localPath = convertUrlPathToLocalPath(path);
            Path filePath = Paths.get(properties.getLocalBasePath(), localPath);
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
            String localPath = convertUrlPathToLocalPath(path);
            Path filePath = Paths.get(properties.getLocalBasePath(), localPath);
            Files.deleteIfExists(filePath);
            
            // 如果是图片，同时删除缩略图
            if (localPath.contains("/origin/")) {
                String thumbPath = localPath.replace("/origin/", "/thumb/");
                Path thumbFilePath = Paths.get(properties.getLocalBasePath(), thumbPath);
                Files.deleteIfExists(thumbFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("本地文件删除失败", e);
        }
    }

    /**
     * 文件是否存在
     */
    @Override
    public boolean exists(String path) {
        String localPath = convertUrlPathToLocalPath(path);
        return Files.exists(Paths.get(properties.getLocalBasePath(), localPath));
    }

    /**
     * 获取文件URL
     * 如果path已经是/files/开头的URL，直接返回
     * 如果是本地路径，转换为/files/开头的URL
     */
    @Override
    public String getUrl(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        // 如果已经是 /files/ 开头的URL，直接返回
        if (path.startsWith("/files/")) {
            return path;
        }
        // 如果是本地路径，转换为 /files/ 开头的URL
        String localPath = convertUrlPathToLocalPath(path);
        return "/files/" + localPath;
    }

    /**
     * 获取文件大小
     */
    @Override
    public long getFileSize(String path) {
        try {
            String localPath = convertUrlPathToLocalPath(path);
            return Files.size(Paths.get(properties.getLocalBasePath(), localPath));
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
            String localPath = convertUrlPathToLocalPath(path);
            return Files.probeContentType(Paths.get(properties.getLocalBasePath(), localPath));
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
            String oldLocalPath = convertUrlPathToLocalPath(oldPath);
            String newLocalPath = convertUrlPathToLocalPath(newPath);
            Path source = Paths.get(properties.getLocalBasePath(), oldLocalPath);
            Path target = Paths.get(properties.getLocalBasePath(), newLocalPath);
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
            String sourceLocalPath = convertUrlPathToLocalPath(sourcePath);
            String targetLocalPath = convertUrlPathToLocalPath(targetPath);
            Files.copy(
                    Paths.get(properties.getLocalBasePath(), sourceLocalPath),
                    Paths.get(properties.getLocalBasePath(), targetLocalPath),
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
     * @param file 要上传的文件
     * @param filename 存储的文件名（相对路径，例如: "avatars/origin/xxx.jpg"）
     * @return 返回访问URL路径（例如: "/files/avatars/origin/xxx.jpg"）
     */
    @Override
    public String uploadFile(File file, String filename) {
        try {
            // Prepare the path where the file will be stored
            Path path = Paths.get(properties.getLocalBasePath(), filename);
            Files.createDirectories(path.getParent());
            // Copy the provided file to the destination path
            Files.copy(file.toPath(), path, StandardCopyOption.REPLACE_EXISTING);
            // 返回访问URL路径
            return "/files/" + filename;
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
