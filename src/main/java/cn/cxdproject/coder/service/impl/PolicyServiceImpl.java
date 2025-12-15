package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.model.dto.CreatePolicyDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdatePolicyDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.PolicyVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.cxdproject.coder.model.entity.Policy;
import cn.cxdproject.coder.mapper.PolicyMapper;
import cn.cxdproject.coder.service.PolicyService;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.NOT_FOUND;

/**
 * Policy 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class PolicyServiceImpl extends ServiceImpl<PolicyMapper, Policy> implements PolicyService {

    private final PolicyMapper policyMapper;
    private final Cache<String, Object> cache;
    public final RedisUtils redisUtils;

    public PolicyServiceImpl(PolicyMapper policyMapper, Cache<String, Object> cache, RedisUtils redisUtils) {
        this.policyMapper = policyMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
    }

    //创建policy
    @Override
    @Loggable(
            type = LogType.POLICY_CREATE,
            value = "Create policy"
    )
    public PolicyVO createPolicyByAdmin(CreatePolicyDTO createDTO) {

        Policy policy = Policy.builder()
                .title(createDTO.getTitle())
                .type(createDTO.getType())
                .content(createDTO.getContent())
                .issueTime(createDTO.getIssueTime())
                .issueUnit(createDTO.getIssueUnit())
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .status(createDTO.getStatus())
                .build();

        policy.setCreatedAt(LocalDateTime.now());
        policy.setUpdatedAt(LocalDateTime.now());

        this.save(policy);
        return toPolicyVO(policy);
    }

    //根据id获取id
    @Override
    @CircuitBreaker(name = "policyGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public PolicyVO getPolicyById(Long policyId) {
        Object store = cache.asMap().get(CaffeineConstants.POLICY + policyId);
        if (store != null) {
            return toPolicyVO((Policy) store);
        } else {
            Policy policy = this.getById(policyId);
            if (policy == null || Boolean.TRUE.equals(policy.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.POLICY + policyId, policy);
            return toPolicyVO(policy);
        }
    }


    //客户端批量查询（游标分页）
    @Override
    @CircuitBreaker(name = "policyGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public List<PolicyVO> getPolicyPage(Long lastId, int size, String keyword) {
        List<Long> ids = policyMapper.selectIds(lastId, size, keyword);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Policy> policies = policyMapper.selectBatchIds(ids);

        Map<Long, Policy> policyMap = policies.stream()
                .collect(Collectors.toMap(Policy::getId, a -> a));

        return ids.stream()
                .map(policyMap::get)
                .filter(Objects::nonNull)
                .map(this::toPolicyVO)
                .collect(Collectors.toList());
    }

    //更新数据（仅限管理端）
    @Override
    @Loggable(
            type = LogType.POLICY_UPDATE,
            value = "Update policy ID: #{#policyId}"
    )
    public PolicyVO updatePolicyByAdmin(Long policyId, UpdatePolicyDTO updateDTO) {
        // 1. 校验是否存在且未被删除
        Policy existing = this.getById(policyId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 2. 执行动态更新
        int updatedRows = policyMapper.updatePolicy(policyId, updateDTO);

        // 3. 若无记录被更新（可能已被删除或并发修改）
        if (updatedRows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 4. 重新加载最新数据（保证一致性）
        Policy updatedPolicy = this.getById(policyId);

        // 5. 更新缓存（注意：缓存的是对象，不是 ID）
        cache.put(CaffeineConstants.POLICY + policyId, updatedPolicy);

        // 6. 返回 VO
        return toPolicyVO(updatedPolicy);
    }


    //删除policy（仅限管理端）
    @Override
    @Loggable(
            type = LogType.POLICY_DELETE,
            value = "Delete policy by ID: #{#policyId}"
    )
    public void deletePolicyByAdmin(Long policyId) {
        boolean updated = policyMapper.update(null,
                Wrappers.<Policy>lambdaUpdate()
                        .set(Policy::getDeleted, true)
                        .eq(Policy::getId, policyId)
                        .eq(Policy::getDeleted, false)
        ) > 0;

        if (!updated) {
            Policy policy = this.getById(policyId);
            if (policy == null || Boolean.TRUE.equals(policy.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.POLICY + policyId);
    }

    @Override
    public PolicyVO toPolicyVO(Policy policy) {
        if (policy == null) {
            return null;
        }
        return PolicyVO.builder()
                .id(policy.getId())
                .title(policy.getTitle())
                .type(policy.getType())
                .issueUnit(policy.getIssueUnit())
                .issueTime(policy.getIssueTime())
                .content(policy.getContent())
                .image(policy.getImage())
                .thumbImage(policy.getThumbImage())
                .cover(policy.getImage()) // 封面图片，与image相同
                .thumbCover(policy.getThumbImage()) // 缩略封面，与thumbImage相同
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .status(policy.getStatus())
                .build();
    }

    //根据id查询的降级接口
    @Override
    public PolicyVO getByIdFallback(Long id, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        Object store = cache.getIfPresent(CaffeineConstants.POLICY + id);
        if (store != null) {
            return toPolicyVO((Policy) store);
        }
        return null;
    }

    //客户端批量查询降级接口
    @Override
    public List<PolicyVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        try {
            // 从 Redis 获取缓存的全量文章（假设是 ArticleVO[] 的 JSON）
            String json = (String) redisUtils.get(TaskConstants.POLICY);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            PolicyVO[] array = JsonUtils.fromJson(json, PolicyVO[].class);
            if (array == null || array.length == 0) {
                return Collections.emptyList();
            }

            int take = Math.min(size, array.length);
            return new ArrayList<>(Arrays.asList(array).subList(0, take));

        } catch (Exception ex) {
            log.error("Fallback failed", ex);
            return Collections.emptyList();
        }
    }

    //管理端分页查询
    @Override
    public Page<PolicyVO> getPolicyPageAdmin(Page<Policy> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        // 获取当前页的数据和总记录数
        List<Policy> policys = policyMapper.getAdminPage(keyword, offset, size);
        Long total = policyMapper.getTotal(keyword);

        List<PolicyVO> voList = policys.stream()
                .map(this::toPolicyVO)
                .collect(Collectors.toList());

        return new Page<PolicyVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }
}
