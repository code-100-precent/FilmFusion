package cn.cxdproject.coder.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 创建模块DTO
 * 
 * @author Hibiscus-code-generate
 */
@Data
public class CreateModuleDTO {
    
    @NotBlank(message = "模块名称不能为空")
    private String name;
    
    private String description;
}
