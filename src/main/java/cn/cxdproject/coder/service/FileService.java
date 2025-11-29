package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
   FileVO imageUpload(MultipartFile file);
}
