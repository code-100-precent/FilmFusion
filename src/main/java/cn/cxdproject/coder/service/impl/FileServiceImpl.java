package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.storage.LocalStorageService;
import cn.cxdproject.coder.common.storage.MinioStorageService;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    private final MinioStorageService minioStorageService;
    private final LocalStorageService localStorageService;

    public FileServiceImpl(MinioStorageService minioStorageService, LocalStorageService localStorageService) {
        this.minioStorageService = minioStorageService;
        this.localStorageService = localStorageService;
    }


    @Override
    public FileVO imageUpload(MultipartFile file) {
        FileVO fileVO = minioStorageService.upload(file);
        return fileVO;
    }
}
