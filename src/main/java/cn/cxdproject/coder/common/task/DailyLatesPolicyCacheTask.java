package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.mapper.PolicyMapper;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.Policy;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.PolicyVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
//Policy的定时任务（用于分页降级时查询的数据）
@Component
@Slf4j
public class DailyLatesPolicyCacheTask {

    private final PolicyMapper policyMapper;
    private final RedisUtils redisUtils;

    public DailyLatesPolicyCacheTask(PolicyMapper policyMapper, RedisUtils redisUtils) {
        this.policyMapper = policyMapper;
        this.redisUtils = redisUtils;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatest10Locations() {
        try {
            // 1. 从数据库查询最新10条
            List<Policy> latestPolicys = policyMapper.selectLatest10();

            if (latestPolicys == null || latestPolicys.isEmpty()) {
                log.warn("未查到政策数据，跳过缓存");
                return;
            }

            // 2. 转为VO列表
            List<PolicyVO> voList = latestPolicys.stream()
                    .map(this::toPolicyVO)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期 25 小时
            redisUtils.set(
                    TaskConstants.POLICY,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条地址到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

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
}
