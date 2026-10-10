package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 前端直传完成后的确认请求DTO
 */
@Data
public class ConfirmFileDTO {

    /**
     * 原图对象键，例如 origin/xxx.jpg
     */
    @NotBlank(message = "原图对象键不能为空")
    private String originKey;

    /**
     * 缩略图对象键，无缩略图时为空
     */
    private String thumbKey;
}