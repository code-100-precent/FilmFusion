package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.CreatePolicyDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdatePolicyDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.PolicyVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.Policy;

import java.util.List;

/**
 * Policy 服务接口
 * 提供政策内容的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface PolicyService extends IService<Policy> {

    /**
     * 管理员创建政策
     */
    PolicyVO createPolicyByAdmin(CreatePolicyDTO createDTO);

    /**
     * 根据ID获取政策详情
     */
    PolicyVO getPolicyById(Long policyId);

    /**
     * 分页获取政策列表（按时间倒序，公开接口）
     */
    List<PolicyVO> getPolicyPage(Long lastId, int size, String keyword);

    /**
     * 管理员更新政策
     */
    PolicyVO updatePolicyByAdmin(Long policyId, UpdatePolicyDTO updateDTO);

    /**
     * 管理员删除政策（逻辑删除）
     */
    void deletePolicyByAdmin(Long policyId);

    /**
     * 将Policy实体转换为PolicyVO
     */
    PolicyVO toPolicyVO(Policy policy);

    /**
     * 获取政策详情的降级方法
     */
    PolicyVO getByIdFallback(Long id, Throwable e);

    /**
     * 分页查询政策列表的降级方法
     */
    List<PolicyVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    /**
     * 管理员分页查询政策
     */
    Page<PolicyVO> getPolicyPageAdmin(Page<Policy> page, String keyword);
}
