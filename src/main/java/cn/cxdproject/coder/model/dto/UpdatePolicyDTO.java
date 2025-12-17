package cn.cxdproject.coder.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
public class UpdatePolicyDTO {
    @NotNull
    private String title;

    /**
     * 政策类型："省级" | "市级"
     */
    @NotNull
    private String type;

    /**
     * 发布单位
     */
    @NotNull
    private String issueUnit;

    /**
     * 发布时间
     */
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime issueTime;

    /**
     * 内容
     */
    @NotNull
    private String content;


    private String image;

    /**
     * 压缩图片
     */

    private String thumbImage;

    private Boolean status;
}
