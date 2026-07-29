package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建每日行程DTO
 *
 * @author Hibiscus-code-generate
 */
@Data
public class CreateDayDTO {

    /**
     * 旅游每日专题名称
     */
    @NotBlank(message = "每日专题名称不能为空")
    private String name;

    /**
     * 第几天
     */
    @NotBlank(message = "天数不能为空")
    private String day;

    /**
     * 该天的景点列表
     */
    @Valid
    @NotNull(message = "景点列表不能为空")
    private List<CreateAttractionDTO> attractions;
}
