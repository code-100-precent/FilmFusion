package cn.cxdproject.coder.common.storage;

import cn.cxdproject.coder.model.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件存储适配器
 */
public interface FileStorageAdapter {

    /**
     * 上传文件
     */
    FileVO upload(MultipartFile file);

    /**
     * 上传文件
     */
    FileVO upload(String prefix, MultipartFile file);

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

    /**
     * 生成 PUT 预签名上传地址，供浏览器直传使用
     *
     * @param key           对象键，例如 origin/xxx.jpg
     * @param expireSeconds 有效期（秒）
     * @return 可直接 PUT 的完整地址
     */
    String generatePresignedPutUrl(String key, int expireSeconds);
}