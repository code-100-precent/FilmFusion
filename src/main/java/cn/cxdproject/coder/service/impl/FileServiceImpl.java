package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.common.storage.FileStorageAdapter;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

    private final FileStorageAdapter fileStorageAdapter;

    public FileServiceImpl(FileStorageAdapter fileStorageAdapter) {
        this.fileStorageAdapter = fileStorageAdapter;
    }

    @Override
//    @Loggable(
//            type = LogType.FILE_UPLOAD,
//            value = "upload file"
//    )
    public FileVO imageUpload(MultipartFile file) {
            FileVO fileVO = fileStorageAdapter.upload(file);
            return fileVO;
    }
}
