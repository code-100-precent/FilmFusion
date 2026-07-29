package cn.cxdproject.coder.model.dto;

import lombok.Data;

/**
 * 更新景点DTO
 *
 * @author Hibiscus-code-generate
 */
@Data
public class UpdateAttractionDTO {

    /**
     * Attraction ID（更新时必传）
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 亮点
     */
    private String highlights;

    /**
     * 附近景点ID（多个用逗号分隔）
     */
    private String locationId;

    /**
     * 相关影视作品ID（多个用逗号分隔）
     */
    private String dramaId;

    /**
     * 附近酒店ID（多个用逗号分隔）
     */
    private String hotelId;

    /**
     * 图片url(第一张为封面)
     */
    private String image;

    /**
     * 压缩后图片url(第一张为封面)
     */
    private String thumbImage;
}
