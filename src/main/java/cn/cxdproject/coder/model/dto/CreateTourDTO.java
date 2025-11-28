package cn.cxdproject.coder.model.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.checkerframework.checker.units.qual.N;

import javax.validation.constraints.NotBlank;

@Data
public class CreateTourDTO {

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
     * 封面
     */
    @NotBlank
    private String cover;

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

    private String thumbCover;

    private String thumbImage;

    private String longitude;

    private String latitude;

    private String locationId;

}
