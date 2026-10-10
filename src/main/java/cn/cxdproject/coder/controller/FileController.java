package cn.cxdproject.coder.controller;


import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.model.dto.ConfirmFileDTO;
import cn.cxdproject.coder.model.dto.PresignRequestDTO;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.model.vo.PresignVO;
import cn.cxdproject.coder.service.FileService;
import cn.cxdproject.coder.service.impl.FileServiceImpl;
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

    public final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping
    public ApiResponse imageUpload(@RequestPart("file") MultipartFile file) throws Exception {
            FileVO fileVO = fileService.imageUpload(file);
            return ApiResponse.success(fileVO);
    }

    /**
     * 签发前端直传 COS 的 PUT 预签名地址
     */
    @PostMapping("/presign")
    public ApiResponse<PresignVO> presign(@Valid @RequestBody PresignRequestDTO dto) {
        return ApiResponse.success(fileService.presign(dto));
    }

    /**
     * 前端直传完成后确认，返回最终访问地址
     */
    @PostMapping("/confirm")
    public ApiResponse<FileVO> confirm(@Valid @RequestBody ConfirmFileDTO dto) {
        return ApiResponse.success(fileService.confirm(dto));
    }
}
