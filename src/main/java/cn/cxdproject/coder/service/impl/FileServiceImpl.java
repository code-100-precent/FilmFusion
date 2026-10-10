package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.storage.FileStorageAdapter;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.model.dto.ConfirmFileDTO;
import cn.cxdproject.coder.model.dto.PresignRequestDTO;
import cn.cxdproject.coder.model.vo.FileVO;
import cn.cxdproject.coder.model.vo.PresignVO;
import cn.cxdproject.coder.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.VALIDATION_ERROR;

@Service
public class FileServiceImpl implements FileService {

    /**
     * 预签名地址有效期（秒）
     */
    private static final int PRESIGN_EXPIRE_SECONDS = 600;

    /**
     * 原图对象目录前缀
     */
    private static final String ORIGIN_PREFIX = "origin/";

    /**
     * 缩略图对象目录前缀
     */
    private static final String THUMB_PREFIX = "thumb/";

    private final FileStorageAdapter fileStorageAdapter;

    public FileServiceImpl(FileStorageAdapter fileStorageAdapter) {
        this.fileStorageAdapter = fileStorageAdapter;
    }

    @Override
    public FileVO imageUpload(MultipartFile file) {
            FileVO fileVO = fileStorageAdapter.upload(file);
            return fileVO;
    }

    @Override
    public PresignVO presign(PresignRequestDTO dto) {
        String baseName = buildBaseName(dto.getFilename());
        String originKey = ORIGIN_PREFIX + baseName;
        String thumbKey = Boolean.TRUE.equals(dto.getNeedThumb()) ? THUMB_PREFIX + baseName : null;

        return PresignVO.builder()
                .originKey(originKey)
                .originUploadUrl(fileStorageAdapter.generatePresignedPutUrl(originKey, PRESIGN_EXPIRE_SECONDS))
                .originUrl(fileStorageAdapter.getUrl(originKey))
                .thumbKey(thumbKey)
                .thumbUploadUrl(thumbKey == null ? null
                        : fileStorageAdapter.generatePresignedPutUrl(thumbKey, PRESIGN_EXPIRE_SECONDS))
                .thumbUrl(thumbKey == null ? null : fileStorageAdapter.getUrl(thumbKey))
                .build();
    }

    @Override
    public FileVO confirm(ConfirmFileDTO dto) {
        String originKey = normalizeKey(dto.getOriginKey(), ORIGIN_PREFIX);
        String thumbKey = dto.getThumbKey() == null || dto.getThumbKey().trim().isEmpty()
                ? null : normalizeKey(dto.getThumbKey(), THUMB_PREFIX);

        if (!fileStorageAdapter.exists(originKey)) {
            throw new BusinessException(VALIDATION_ERROR.code(), "上传未完成，原图不存在");
        }

        return new FileVO(
                fileStorageAdapter.getUrl(originKey),
                thumbKey == null ? null : fileStorageAdapter.getUrl(thumbKey));
    }

    /**
     * 校验对象键必须落在指定目录下，防止路径遍历
     */
    private String normalizeKey(String key, String expectedPrefix) {
        String normalized = key.startsWith("/") ? key.substring(1) : key;
        if (!normalized.startsWith(expectedPrefix) || normalized.contains("..")) {
            throw new BusinessException(VALIDATION_ERROR.code(), "非法的对象键");
        }
        return normalized;
    }

    private String buildBaseName(String filename) {
        String safeFilename = filename == null ? "file" : filename.replaceAll("[/\\\\]", "_");
        return System.currentTimeMillis() + "_" + safeFilename;
    }
}