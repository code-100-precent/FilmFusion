package cn.cxdproject.coder.common.storage;

import cn.cxdproject.coder.model.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件存储适配器
 */
public interface FileStorageAdapter {

    /**
     * 上传文件
     */
    ImageVO upload(MultipartFile file);

    /**
     * 上传文件
     */
    ImageVO upload(String prefix, MultipartFile file);

    /**
     * 下载文件
     */
    byte[] download(String path);

    /**
     * 删除文件
     */
    void delete(String path);

    /**
     * 判断文件是否存在
     */
    boolean exists(String path);

    /**
     * 获取文件访问地址
     */
    String getUrl(String path);

    /**
     * 获取文件大小
     */
    long getFileSize(String path);

    /**
     * 获取文件类型
     */
    String getContentType(String path);

    /**
     * 重命名文件
     */
    void rename(String oldPath, String newPath);

    /**
     * 复制文件
     */
    void copy(String sourcePath, String targetPath);

    /**
     * 移动文件
     */
    void move(String sourcePath, String targetPath);

    /**
     * 上传文件
     */
    String uploadFile(File file, String filename);
}