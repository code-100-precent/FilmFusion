package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateModuleDTO;
import cn.cxdproject.coder.model.dto.UpdateModuleDTO;
import cn.cxdproject.coder.model.entity.Module;
import cn.cxdproject.coder.model.vo.ModuleVO;
import cn.cxdproject.coder.mapper.ModuleMapper;
import cn.cxdproject.coder.service.ModuleService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Module 服务实现类
 * @author Hibiscus-code-generate
 */
@Slf4j
@Service
public class ModuleServiceImpl extends ServiceImpl<ModuleMapper, Module> implements ModuleService {

    private final ModuleMapper moduleMapper;

    public ModuleServiceImpl(ModuleMapper moduleMapper) {
        this.moduleMapper = moduleMapper;
    }

    @Override
    @Loggable(
            type = LogType.MODULE_CREATE,
            value = "Create module"
    )
    public ModuleVO createModuleByAdmin(CreateModuleDTO createDTO) {
        Module module = Module.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .build();

        module.setCreatedAt(LocalDateTime.now());
        module.setUpdatedAt(LocalDateTime.now());
        module.setDeleted(false);

        this.save(module);
        return toModuleVO(module);
    }

    @Override
    public ModuleVO getModuleById(Long moduleId) {
        Module module = this.getById(moduleId);
        if (module == null || Boolean.TRUE.equals(module.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }
        return toModuleVO(module);
    }

    @Override
    public List<ModuleVO> getModulePage(Long lastId, int size, String keyword) {
        List<Long> ids = moduleMapper.selectIds(lastId, size, keyword);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Module> modules = moduleMapper.selectBatchIds(ids);

        Map<Long, Module> moduleMap = modules.stream()
                .collect(Collectors.toMap(Module::getId, m -> m));

        return ids.stream()
                .map(moduleMap::get)
                .filter(Objects::nonNull)
                .map(this::toModuleVO)
                .collect(Collectors.toList());
    }

    @Override
    @Loggable(
            type = LogType.MODULE_UPDATE,
            value = "Update module ID: #{#moduleId}"
    )
    public ModuleVO updateModuleByAdmin(Long moduleId, UpdateModuleDTO updateDTO) {
        Module existing = this.getById(moduleId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        int updatedRows = moduleMapper.updateModule(moduleId, updateDTO);

        if (updatedRows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        Module updatedModule = this.getById(moduleId);
        return toModuleVO(updatedModule);
    }

    @Override
    @Loggable(
            type = LogType.MODULE_DELETE,
            value = "Delete module by ID: #{#moduleId}"
    )
    public void deleteModuleByAdmin(Long moduleId) {
        boolean updated = moduleMapper.update(null,
                Wrappers.<Module>lambdaUpdate()
                        .set(Module::getDeleted, true)
                        .eq(Module::getId, moduleId)
                        .eq(Module::getDeleted, false)
        ) > 0;

        if (!updated) {
            Module module = this.getById(moduleId);
            if (module == null || Boolean.TRUE.equals(module.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
    }

    @Override
    public ModuleVO toModuleVO(Module module) {
        if (module == null) {
            return null;
        }
        return ModuleVO.builder()
                .id(module.getId())
                .name(module.getName())
                .description(module.getDescription())
                .createdAt(module.getCreatedAt())
                .updatedAt(module.getUpdatedAt())
                .build();
    }

    @Override
    public Page<ModuleVO> getModulePageAdmin(Page<Module> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Module> modules = moduleMapper.getAdminPage(keyword, offset, size);
        Long total = moduleMapper.getTotal(keyword);

        List<ModuleVO> voList = modules.stream()
                .map(this::toModuleVO)
                .collect(Collectors.toList());

        return new Page<ModuleVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }
}
