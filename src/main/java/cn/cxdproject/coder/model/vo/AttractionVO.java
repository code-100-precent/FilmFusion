package cn.cxdproject.coder.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 景点视图对象
 *
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttractionVO {

    /**
     * 景点ID
     */
    private Long id;

    /**
     * 关联day_id
     */
    private Long dayId;

    /**
     * 关联tour_id
     */
    private Long tourId;

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

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
