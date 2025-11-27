package cn.cxdproject.coder.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HotelVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 住宿名称
     */
    private String name;

    /**
     * 介绍
     */
    private String description;

    /**
     * 地址
     */
    private String address;

    /**
     * 负责人名称
     */
    private String managerName;

    /**
     * 负责人电话
     */
    private String managerPhone;

    /**
     * 封面
     */
    private String cover;

    /**
     * 图片
     */
    private String image;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String thumbCover;

    private String thumbImage;


}
