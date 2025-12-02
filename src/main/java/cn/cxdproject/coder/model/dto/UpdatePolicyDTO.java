package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class UpdatePolicyDTO {
    @NotBlank
    private String title;

    /**
     * 政策类型："省级" | "市级"
     */
    @NotBlank
    private String type;

    /**
     * 发布单位
     */
    @NotBlank
    private String issueUnit;

    /**
     * 发布时间
     */
    @NotBlank
    private LocalDateTime issueTime;

    /**
     * 内容
     */
    @NotBlank
    private String content;


    private String image;

    /**
     * 压缩图片
     */

    private String thumbImage;
}
