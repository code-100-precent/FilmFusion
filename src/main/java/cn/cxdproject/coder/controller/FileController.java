package cn.cxdproject.coder.controller;


import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateFeedbackDTO;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.FeedbackVO;
import cn.cxdproject.coder.model.vo.ImageVO;
import cn.cxdproject.coder.service.impl.FileServiceImpl;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/file")
@Slf4j
@Validated
public class FileController {

    public final FileServiceImpl fileService;

    public FileController(FileServiceImpl fileService) {
        this.fileService = fileService;
    }

    @PutMapping
    public ApiResponse imageUpload(@RequestPart("file") MultipartFile file) throws Exception {
        ImageVO imageVO = fileService.imageUpload(file);
        return ApiResponse.success(imageVO);
    }
}
