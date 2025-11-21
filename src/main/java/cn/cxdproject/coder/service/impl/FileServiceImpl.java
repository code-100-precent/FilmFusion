package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.storage.LocalStorageService;
import cn.cxdproject.coder.common.storage.MinioStorageService;
import cn.cxdproject.coder.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    private final LocalStorageService localStorageService;

    public FileServiceImpl(LocalStorageService localStorageService) {

        this.localStorageService = localStorageService;
    }

    @Override
    public String imageUpload(MultipartFile file) {
        String url = localStorageService.upload(file);
        return url;
    }
}
