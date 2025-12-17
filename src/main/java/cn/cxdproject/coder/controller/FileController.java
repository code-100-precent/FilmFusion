package cn.cxdproject.coder.controller;


import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.service.FileService;
import cn.cxdproject.coder.service.impl.FileServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@Slf4j
@Validated
public class FileController {

    public final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ApiResponse imageUpload(@RequestPart("file") MultipartFile file) throws Exception {
            FileVO fileVO = fileService.imageUpload(file);
            return ApiResponse.success(fileVO);
    }
}
