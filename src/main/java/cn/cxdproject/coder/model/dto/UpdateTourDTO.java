package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * 更新线路DTO
 *
 * @author Hibiscus-code-generate
 */
@Data
public class UpdateTourDTO {

    /**
     * 体验游名称
     */
    private String name;

    /**
     * 介绍
     */
    private String description;

    /**
     * 每日行程列表（完整替换）
     */
    @Valid
    private List<UpdateDayDTO> days;
}
