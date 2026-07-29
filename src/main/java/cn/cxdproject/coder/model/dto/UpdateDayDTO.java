package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * 更新每日行程DTO
 *
 * @author Hibiscus-code-generate
 */
@Data
public class UpdateDayDTO {

    /**
     * Day ID（更新时必传）
     */
    private Long id;

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
    @Valid
    private List<UpdateAttractionDTO> attractions;
}
