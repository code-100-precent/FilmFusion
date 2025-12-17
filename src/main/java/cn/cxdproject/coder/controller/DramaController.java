package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.CursorPageResponseVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.service.DramaService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 电视剧备案控制器
 * 
 * @author heathcetide
 */
@Slf4j
@RestController
@RequestMapping("/api/drama")
@Validated
public class DramaController {

    private final DramaService dramaService;

    public DramaController(DramaService dramaService) {
        this.dramaService = dramaService;
    }

    /**
     * 获取电视剧备案详情（公开）
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<DramaVO> getDramaById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        DramaVO dramaVO = dramaService.getDramaByIdWithTimeout(id);
        return ApiResponse.success(dramaVO);
    }

    /**
     * 分页获取电视剧备案列表（按时间倒序，公开）
     */
    @GetMapping("/page")
    @PublicAccess
    public CursorPageResponseVO<DramaVO> page(
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

        List<DramaVO> list = dramaService.getDramaPageWithTimeout(lastId, size, keyword);

        String nextCursor = null;
        if (list.size() == size && !list.isEmpty()) {
            nextCursor = String.valueOf(list.get(list.size() - 1).getId());
        }

        return new CursorPageResponseVO<>(list, nextCursor);
    }

    /**
     * 管理员更新电视剧备案
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<DramaVO> updateDramaByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdateDramaDTO updateDTO) {
        DramaVO dramaVO = dramaService.updateDramaByAdmin(id, updateDTO);
        return ApiResponse.success(dramaVO);
    }

    //管理员分页
    @GetMapping("/admin/page")
    public PageResponse<DramaVO> getDramaPageAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Drama> page = new Page<>(current, size);
        Page<DramaVO> dramaPage = dramaService.getDramaPageAdmin(page, keyword);

        return PageResponse.of(
                (int) dramaPage.getCurrent(),
                (int) dramaPage.getSize(),
                dramaPage.getTotal(),
                dramaPage.getRecords()
        );
    }

    /**
     * 创建电视剧备案
     */
    @PostMapping("/admin/create")
    public ApiResponse<DramaVO> createDrama(@Valid @RequestBody CreateDramaDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        DramaVO dramaVO = dramaService.createDramaByAdmin(currentUser.getId(), createDTO);
        return ApiResponse.success(dramaVO);
    }


    /**
     * 管理员删除电视剧备案
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteDramaByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        dramaService.deleteDramaByAdmin(id);
        return ApiResponse.success();
    }

}
