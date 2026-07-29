package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建线路DTO
 *
 * @author Hibiscus-code-generate
 */
@Data
public class CreateTourDTO {

    /**
     * 体验游名称
     */
    @NotBlank(message = "线路名称不能为空")
    private String name;

    /**
     * 介绍
     */
    @NotBlank(message = "线路介绍不能为空")
    private String description;

    /**
     * 每日行程列表
     */
    @Valid
    @NotNull(message = "行程列表不能为空")
    private List<CreateDayDTO> days;
}
