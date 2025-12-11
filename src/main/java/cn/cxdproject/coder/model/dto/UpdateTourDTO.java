package cn.cxdproject.coder.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateTourDTO {
    /**
     * 体验游名称
     */
    @NotBlank
    private String name;

    /**
     * 介绍
     */
    @NotBlank
    private String description;

    /**
     * 主题
     */
    @NotBlank
    private String theme;

    /**
     * 特点
     */
    @NotBlank
    private String features;


    /**
     * 交通方式
     */
    @NotBlank
    private String transport;

    /**
     * 周边旅馆
     */
    @NotBlank
    private String hotel;

    /**
     * 美食推荐
     */
    @NotBlank
    private String food;

    /**
     * images
     */
    private String image;

    private String thumbImage;

    private String locationId;

    private String dramaId;

    private Integer deleted;
}
