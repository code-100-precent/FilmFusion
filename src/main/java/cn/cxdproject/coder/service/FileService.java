package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
   ImageVO imageUpload(MultipartFile file);
}
