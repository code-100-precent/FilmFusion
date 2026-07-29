package cn.cxdproject.coder.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 每日行程视图对象
 *
 * @author Hibiscus-code-generate
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DayVO {

    /**
     * Day ID
     */
    private Long id;

    /**
     * 关联tour_id
     */
    private Long tourId;

    /**
     * 旅游每日专题名称
     */
    private String name;

    /**
     * 第几天
     */
    private String day;

    /**
     * 该天的景点列表
     */
    private List<AttractionVO> attractions;

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
