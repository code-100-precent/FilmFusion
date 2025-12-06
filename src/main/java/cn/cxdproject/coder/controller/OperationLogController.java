package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.model.dto.UpdateArticleDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.OperationLog;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.OperationLogVO;
import cn.cxdproject.coder.service.OperationLogService;
import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageRequest;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * OperationLog 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/operationlog")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    //分页获取操作信息
    @GetMapping("/admin/page")
    public PageResponse<OperationLogVO> getOperationPageAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<OperationLog> page = new Page<>(current, size);
        Page<OperationLogVO> operationPage = operationLogService.getOperationPage(page, keyword);

        return PageResponse.of(
                (int) operationPage.getCurrent(),
                (int) operationPage.getSize(),
                operationPage.getTotal(),
                operationPage.getRecords()
        );
    }

    //根据id获取操作信息
    @GetMapping("/admin/{id}")
    @PublicAccess
    public ApiResponse<OperationLogVO> getOperationById(@PathVariable @NotNull(message = "记录ID不能为空") Long id) {
       OperationLogVO operationLogVO = operationLogService.getOperationById(id);
        return ApiResponse.success(operationLogVO);
    }


    /**
     * 管理员删除文章
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteOperation(@PathVariable @NotNull(message = "记录ID不能为空") Long id) {
        operationLogService.deleteArticle(id);
        return ApiResponse.success();
    }


}
