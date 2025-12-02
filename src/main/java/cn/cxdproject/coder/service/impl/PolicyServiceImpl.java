package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
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

    @Override
    public PolicyVO createPolicyByAdmin(CreatePolicyDTO createDTO) {
        if (createDTO.getImage() == null) {
            createDTO.setImage(Constants.DEFAULT_COVER);
            createDTO.setThumbImage(Constants.DEFAULT_THUMB_COVER);
        }

        Policy policy = Policy.builder()
                .title(createDTO.getTitle())
                .type(createDTO.getType())
                .content(createDTO.getContent())
                .issueTime(createDTO.getIssueTime())
                .issueUnit(createDTO.getIssueUnit())
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .build();

        this.save(policy);
        return toPolicyVO(policy);
    }

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

    @Override
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    public PolicyVO updatePolicyByAdmin(Long policyId, UpdatePolicyDTO updateDTO) {
        Policy policy = this.getById(policyId);
        if (policy == null || Boolean.TRUE.equals(policy.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getTitle() != null) policy.setTitle(updateDTO.getTitle());
        if (updateDTO.getType() != null) policy.setType(updateDTO.getType());
        if (updateDTO.getIssueUnit() != null) policy.setIssueUnit(updateDTO.getIssueUnit());
        if (updateDTO.getIssueTime() != null) policy.setIssueTime(updateDTO.getIssueTime());
        if (updateDTO.getContent() != null) policy.setContent(updateDTO.getContent());
        if (updateDTO.getImage() != null) policy.setImage(updateDTO.getImage());
        if (updateDTO.getThumbImage() != null) policy.setThumbImage(updateDTO.getThumbImage());

        cache.asMap().put(CaffeineConstants.POLICY + policyId, policy);
        this.updateById(policy);
        return toPolicyVO(policy);
    }

    @Override
    @Bulkhead(name = "delete", type = Bulkhead.Type.SEMAPHORE)
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
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    @Override
    public PolicyVO getByIdFallback(Long id, Throwable e) {
        Object store;
        store = cache.getIfPresent(CaffeineConstants.POLICY + id);
        if (store != null) {
            return toPolicyVO((Policy) store);
        }
        store = redisUtils.get(RedisKeyConstants.POLICY + id);
        if (store != null) {
            Policy policy = JsonUtils.fromJson((String) store, Policy.class);
            return toPolicyVO(policy);
        }
        return null;
    }

    @Override
    public List<PolicyVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

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

    @Override
    public Page<PolicyVO> getPolicyPageAdmin(Page<Policy> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Policy> policies = policyMapper.getAdminPage(keyword, offset, size);

        List<PolicyVO> voList = policies.stream()
                .map(this::toPolicyVO)
                .collect(Collectors.toList());

        return new Page<PolicyVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }
}
