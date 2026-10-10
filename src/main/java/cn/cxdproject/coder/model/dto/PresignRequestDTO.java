package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 前端直传 COS 预签名请求DTO
 */
@Data
public class PresignRequestDTO {

    /**
     * 原始文件名（含扩展名），用于生成对象键
     */
    @NotBlank(message = "文件名不能为空")
    private String filename;

    /**
     * 上传内容的 MIME 类型
     */
    @NotBlank(message = "文件类型不能为空")
    private String contentType;

    /**
     * 是否需要同时生成缩略图上传凭证
     */
    private Boolean needThumb;
}