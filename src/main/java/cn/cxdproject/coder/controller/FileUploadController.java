package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 *
 * @author CampusGuide
 */
@RestController
@RequestMapping("/api/common")
@Api(tags = "文件上传模块")
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    /**
     * 获取项目根目录
     */
    private static String getProjectRoot() {
        String userDir = System.getProperty("user.dir");
        return userDir;
    }

    /**
     * 获取上传目录路径
     */
    private static Path getUploadDir(String type) {
        String projectRoot = getProjectRoot();
        Path uploadDir = Paths.get(projectRoot, "tmp", "uploads", type);
        return uploadDir;
    }

    /**
     * 通用文件上传接口
     */
    @PostMapping("/upload")
    @ApiOperation("通用文件上传")
    public ApiResponse<String> uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "type", required = false, defaultValue = "image") String type) {
        try {
            if (file.isEmpty()) {
                return ApiResponse.error("文件不能为空");
            }

            // 创建上传目录（基于项目根目录）
            Path uploadDir = getUploadDir(type);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传目录: {}", uploadDir.toAbsolutePath());
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadDir.resolve(filename);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 返回访问URL（相对路径）
            String fileUrl = "/api/files/" + type + "/" + filename;
            
            log.info("文件上传成功: {} -> {} (存储路径: {})", originalFilename, fileUrl, filePath.toAbsolutePath());
            return ApiResponse.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 上传头像（头像专用接口）
     */
    @PostMapping("/upload/avatar")
    @ApiOperation("上传头像")
    public ApiResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return uploadFile(file, "avatar");
    }

    /**
     * 文件访问接口（通过WebConfig配置的静态资源访问）
     * 实际访问路径：/api/files/{type}/{filename}
     * 映射到：./tmp/uploads/{type}/{filename}
     */
    @GetMapping("/files/{type}/{filename:.+}")
    @ApiOperation("文件访问")
    public org.springframework.http.ResponseEntity<byte[]> getFile(@PathVariable String type, 
                                                                   @PathVariable String filename) {
        try {
            Path filePath = getUploadDir(type).resolve(filename);
            if (!Files.exists(filePath)) {
                log.warn("文件不存在: {}", filePath.toAbsolutePath());
                return org.springframework.http.ResponseEntity.notFound().build();
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                // 根据文件扩展名判断类型
                if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.toLowerCase().endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.toLowerCase().endsWith(".gif")) {
                    contentType = "image/gif";
                } else {
                    contentType = "application/octet-stream";
                }
            }

            return org.springframework.http.ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                    .body(fileBytes);
        } catch (IOException e) {
            log.error("文件读取失败", e);
            return org.springframework.http.ResponseEntity.status(500).build();
        }
    }
}


