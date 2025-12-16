package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * File 服务接口
 * 提供文件上传等相关能力
 * @author Hibiscus-code-generate
 */
public interface FileService {

    /**
     * 上传图片文件
     *
     * @param file 图片文件
     * @return 上传后的文件信息
     */
    FileVO imageUpload(MultipartFile file);
}
