package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateModuleDTO;
import cn.cxdproject.coder.model.dto.UpdateModuleDTO;
import cn.cxdproject.coder.model.entity.Module;
import cn.cxdproject.coder.model.vo.ModuleVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Module 服务接口
 * @author Hibiscus-code-generate
 */
public interface ModuleService extends IService<Module> {
    
    /**
     * 管理员创建模块
     */
    ModuleVO createModuleByAdmin(CreateModuleDTO createDTO);

    /**
     * 根据ID获取模块详情
     */
    ModuleVO getModuleById(Long moduleId);

    /**
     * 分页获取模块列表
     */
    List<ModuleVO> getModulePage(Long lastId, int size, String keyword);

    /**
     * 管理员更新模块信息
     */
    ModuleVO updateModuleByAdmin(Long moduleId, UpdateModuleDTO updateDTO);

    /**
     * 管理员删除模块（逻辑删除）
     */
    void deleteModuleByAdmin(Long moduleId);

    /**
     * 将Module实体转换为ModuleVO
     */
    ModuleVO toModuleVO(Module module);

    /**
     * 管理员分页查询模块
     */
    Page<ModuleVO> getModulePageAdmin(Page<Module> page, String keyword);
}
