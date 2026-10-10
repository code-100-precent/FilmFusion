package cn.cxdproject.coder.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前端直传 COS 的预签名凭证
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PresignVO {

    /**
     * 原图对象键
     */
    private String originKey;

    /**
     * 原图 PUT 预签名地址
     */
    private String originUploadUrl;

    /**
     * 原图最终访问地址
     */
    private String originUrl;

    /**
     * 缩略图对象键，无需缩略图时为空
     */
    private String thumbKey;

    /**
     * 缩略图 PUT 预签名地址，无需缩略图时为空
     */
    private String thumbUploadUrl;

    /**
     * 缩略图最终访问地址，无需缩略图时为空
     */
    private String thumbUrl;
}