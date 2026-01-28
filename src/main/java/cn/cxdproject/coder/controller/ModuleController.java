package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.model.dto.CreateModuleDTO;
import cn.cxdproject.coder.model.dto.UpdateModuleDTO;
import cn.cxdproject.coder.model.entity.Module;
import cn.cxdproject.coder.model.vo.CursorPageResponseVO;
import cn.cxdproject.coder.model.vo.ModuleVO;
import cn.cxdproject.coder.service.ModuleService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 模块控制器
 * 
 * @author Hibiscus-code-generate
 */
@Slf4j
@RestController
@RequestMapping("/api/module")
@Validated
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    /**
     * 获取模块详情（公开）
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<ModuleVO> getModuleById(@PathVariable @NotNull(message = "模块ID不能为空") Long id) {
        ModuleVO moduleVO = moduleService.getModuleById(id);
        return ApiResponse.success(moduleVO);
    }

    /**
     * 分页获取模块列表（按时间倒序，公开）
     */
    @GetMapping("/page")
    @PublicAccess
    public CursorPageResponseVO<ModuleVO> page(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {

        Long lastId = null;
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                lastId = Long.parseLong(cursor.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid cursor");
            }
        }

        List<ModuleVO> list = moduleService.getModulePage(lastId, size, keyword);

        String nextCursor = null;
        if (list.size() == size && !list.isEmpty()) {
            nextCursor = String.valueOf(list.get(list.size() - 1).getId());
        }

        return new CursorPageResponseVO<>(list, nextCursor);
    }

    /**
     * 管理员创建模块
     */
    @PostMapping("/admin/create")
    public ApiResponse<ModuleVO> createModuleByAdmin(@Valid @RequestBody CreateModuleDTO createDTO) {
        ModuleVO moduleVO = moduleService.createModuleByAdmin(createDTO);
        return ApiResponse.success(moduleVO);
    }

    /**
     * 管理员分页查询模块
     */
    @GetMapping("/admin/page")
    public PageResponse<ModuleVO> getModulePageAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Module> page = new Page<>(current, size);
        Page<ModuleVO> modulePage = moduleService.getModulePageAdmin(page, keyword);

        return PageResponse.of(
                (int) modulePage.getCurrent(),
                (int) modulePage.getSize(),
                modulePage.getTotal(),
                modulePage.getRecords()
        );
    }

    /**
     * 管理员更新模块
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<ModuleVO> updateModuleByAdmin(
            @PathVariable @NotNull(message = "模块ID不能为空") Long id,
            @Valid @RequestBody UpdateModuleDTO updateDTO) {
        ModuleVO moduleVO = moduleService.updateModuleByAdmin(id, updateDTO);
        return ApiResponse.success(moduleVO);
    }

    /**
     * 管理员删除模块
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteModuleByAdmin(@PathVariable @NotNull(message = "模块ID不能为空") Long id) {
        moduleService.deleteModuleByAdmin(id);
        return ApiResponse.success();
    }
}
