package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.CreatePolicyDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdatePolicyDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.Policy;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.CursorPageResponseVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.PolicyVO;
import cn.cxdproject.coder.service.PolicyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Policy 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/policy")
public class PolicyController {

    private final PolicyService policyService;

    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }

    /**
     * 新增 Policy 记录
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<PolicyVO> getPolicyById(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        PolicyVO policyVO = policyService.getPolicyByIdWithTimeout(id);
        return ApiResponse.success(policyVO);
    }

    /**
     * 分页获取拍摄场地列表（按时间倒序，公开，只显示可用的）
     */
    @GetMapping("/page")
    @PublicAccess
    public CursorPageResponseVO<PolicyVO> page(
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

        List<PolicyVO> list = policyService.getPolicyPageWithTimeout(lastId, size, keyword);

        String nextCursor = null;
        if (list.size() == size && !list.isEmpty()) {
            nextCursor = String.valueOf(list.get(list.size() - 1).getId());
        }

        return new CursorPageResponseVO<>(list, nextCursor);
    }



    /**
     * 创建拍摄场地
     */
    @PostMapping("/admin/create")
    public ApiResponse<PolicyVO> createPolicy(@Valid @RequestBody CreatePolicyDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.error(401, "未登录");
        }
        PolicyVO policyVO = policyService.createPolicyByAdmin(createDTO);
        return ApiResponse.success(policyVO);
    }

    //管理员分页查询
    @GetMapping("/admin/page")
    public PageResponse<PolicyVO> getPolicyPageAdmin(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {

        Page<Policy> page = new Page<>(current, size);
        Page<PolicyVO> policyPage = policyService.getPolicyPageAdmin(page, keyword);

        return PageResponse.of(
                (int) policyPage.getCurrent(),
                (int) policyPage.getSize(),
                policyPage.getTotal(),
                policyPage.getRecords()
        );
    }

    /**
     * 管理员更新拍摄场地
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<PolicyVO> updatePolicyByAdmin(
            @PathVariable @NotNull(message = "ID不能为空") Long id,
            @Valid @RequestBody UpdatePolicyDTO updateDTO) {
        PolicyVO policyVO = policyService.updatePolicyByAdmin(id, updateDTO);
        return ApiResponse.success(policyVO);
    }

    /**
     * 管理员删除拍摄场地
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deletePolicyByAdmin(@PathVariable @NotNull(message = "ID不能为空") Long id) {
        policyService.deletePolicyByAdmin(id);
        return ApiResponse.success();
    }
}
