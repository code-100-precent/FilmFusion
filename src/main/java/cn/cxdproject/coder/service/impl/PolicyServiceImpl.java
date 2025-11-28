package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.model.dto.CreatePolicyDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdatePolicyDTO;
import cn.cxdproject.coder.model.entity.Location;
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
        if (createDTO.getCover() == null) {
            createDTO.setCover(Constants.DEFAULT_COVER);
        }

        Policy policy = Policy.builder()
                .title(createDTO.getTitle())
                .type(createDTO.getType())
                .content(createDTO.getContent())
                .issueTime(createDTO.getIssueTime())
                .issueUnit(createDTO.getIssueUnit())
                .image(createDTO.getImage())
                .cover(createDTO.getCover())
                .thumbCover(createDTO.getThumbCover())
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
    public Page<PolicyVO> getPolicyPage(Page<Policy> page, String keyword) {

    long current = page.getCurrent();
    long size = page.getSize();
    long offset = (current - 1) * size;

    List<Long> ids = policyMapper.getPagePolicyIds(keyword, offset, size);
    long total = policyMapper.countByKeyword(keyword);

    if (ids.isEmpty()) {
        throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
    }

    // 1. 批量从 Redis 获取缓存
    List<String> keys = ids.stream().map(id -> RedisKeyConstants.POLICY + id).collect(Collectors.toList());
    List<String> cachedJsons = redisUtils.multiGet(keys);

    // 2. 构建Location列表：优先用缓存，缺失的记录 ID
    List<Policy> policys = new ArrayList<>(Collections.nCopies(ids.size(), null));
    List<Long> missingIds = new ArrayList<>();

    for (int i = 0; i < ids.size(); i++) {
        String json = cachedJsons.get(i);
        if (json != null) {
            try {
                policys.set(i, JsonUtils.fromJson(json, Policy.class));
            } catch (Exception e) {
                missingIds.add(ids.get(i));
            }
        } else {
            missingIds.add(ids.get(i));
        }
    }

    if (!missingIds.isEmpty()) {
        List<Policy> dbPolicys = policyMapper.selectBatchIds(missingIds);
        Map<Long, Policy> dbMap = dbPolicys.stream()
                .peek(policy -> {
                    // 回填 Redis 缓存
                    redisUtils.set(
                            RedisKeyConstants.POLICY + policy.getId(),
                            JsonUtils.toJson(policy),
                            Duration.ofMinutes(30)
                    );
                    cache.put(CaffeineConstants.POLICY + policy.getId(), policy);
                })
                .collect(Collectors.toMap(Policy::getId, a -> a));

        // 填回原位置
        for (int i = 0; i < ids.size(); i++) {
            if (policys.get(i) == null) {
                policys.set(i, dbMap.get(ids.get(i)));
            }
        }
    }


        List<PolicyVO> voList = policys.stream()
                .map(this::toPolicyVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Page<PolicyVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);
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
        if (updateDTO.getCover() != null) policy.setCover(updateDTO.getCover());
        if (updateDTO.getImage() != null) policy.setImage(updateDTO.getImage());
        if (updateDTO.getThumbCover() != null) policy.setThumbCover(updateDTO.getThumbCover());
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
                .cover(policy.getCover())
                .image(policy.getImage())
                .thumbCover(policy.getThumbCover())
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
    public Page<PolicyVO> getPageFallback(Page<Policy> page, String keyword, Throwable e) {
        try {
            String json = (String) redisUtils.get(TaskConstants.POLICY);
            if (json == null || json.isEmpty()) {
                return new Page<PolicyVO>()
                        .setCurrent(page.getCurrent())
                        .setSize(page.getSize())
                        .setTotal(0)
                        .setRecords(Collections.emptyList());
            }

            PolicyVO[] array = JsonUtils.fromJson(json, PolicyVO[].class);
            List<PolicyVO> list = array != null ? Arrays.asList(array) : Collections.emptyList();

            long total = list.size();

            return new Page<PolicyVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(total)
                    .setRecords(new ArrayList<>(list));
        } catch (Exception ex) {
            log.error("getPageFallback error", ex);
            return new Page<PolicyVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(0)
                    .setRecords(Collections.emptyList());
        }
    }
}
