package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateHotelDTO {

    /**
     * 住宿名称
     */
    @NotBlank
    private String name;

    /**
     * 介绍
     */
    @NotBlank
    private String description;

    /**
     * 地址
     */
   @NotBlank
    private String address;

    /**
     * 负责人名称
     */
   @NotBlank
    private String managerName;

    /**
     * 负责人电话
     */
   @NotBlank
    private String managerPhone;

    /**
     * 封面
     */
    @NotBlank
    private String cover;

    /**
     * 图片
     */
    private String image;

}
