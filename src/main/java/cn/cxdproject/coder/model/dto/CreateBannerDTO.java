package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateBannerDTO {

    @NotBlank(message = "图片名称不能为空")
    private String imageName;

    @NotBlank(message = "图片地址不能为空")
    private String imageUrl;

    @NotBlank(message = "目标模块不能为空")
    private String targetModule;

    @NotBlank(message = "序列号不能为空")
    private Long sort;
}
