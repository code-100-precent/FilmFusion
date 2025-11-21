package cn.cxdproject.coder.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    String imageUpload(MultipartFile file);
}
