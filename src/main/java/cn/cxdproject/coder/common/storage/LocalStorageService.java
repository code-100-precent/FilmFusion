package cn.cxdproject.coder.common.storage;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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
    public String upload(MultipartFile file) {
        try {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(properties.getLocalBasePath(), filename);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
            return filename;
        } catch (IOException e) {
            throw new RuntimeException("本地文件上传失败", e);
        }
    }

    @Override
    public String upload(String prefix, MultipartFile file) {

        try {
            String filename = System.currentTimeMillis() + "_" + prefix + "/" + file.getOriginalFilename();
            Path path = Paths.get(properties.getLocalBasePath(), filename);
            Files.createDirectories(path.getParent());
            file.transferTo(path.toFile());
            return filename;
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
}
