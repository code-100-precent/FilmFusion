package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.CreatePolicyDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdatePolicyDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.PolicyVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.Policy;

/**
 * Policy 服务接口
 * @author Hibiscus-code-generate
 */
public interface PolicyService extends IService<Policy> {
    PolicyVO createPolicyByAdmin(CreatePolicyDTO createDTO);

    PolicyVO getPolicyById(Long policyId);

    Page<PolicyVO> getPolicyPage(Page<Policy> page, String keyword);

    PolicyVO updatePolicyByAdmin(Long policyId, UpdatePolicyDTO updateDTO);

    void deletePolicyByAdmin(Long policyId);

    PolicyVO toPolicyVO(Policy policy);

    PolicyVO getByIdFallback(Long id,Throwable e);

    Page<PolicyVO> getPageFallback(Page<Policy> page, String keyword, Throwable e);

}
