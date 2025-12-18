package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
//Drama的定时任务（用于分页降级时查询的数据）
@Component
@Slf4j
public class DailyLatesDramaCacheTask {

    private final DramaMapper dramaMapper;
    private final RedisUtils redisUtils;


    public DailyLatesDramaCacheTask(DramaMapper dramaMapper,
                                    RedisUtils redisUtils) {
        this.dramaMapper = dramaMapper;
        this.redisUtils = redisUtils;
    }


    // 降低执行频率：每5分钟执行一次，避免频繁操作
    @Scheduled(cron = "0 * * * * ?")
    public void cacheLatestDramaPage() {
        long startTime = System.currentTimeMillis();
        try {
            // 1. 从数据库查询最新10条
            List<Drama> latestDramas = dramaMapper.selectLatest10();

            if (latestDramas == null || latestDramas.isEmpty()) {
                log.warn("未查到影视数据，跳过缓存");
                return;
            }

            // 2. 转为 VO 列表
            List<DramaVO> voList = latestDramas.stream()
                    .map(this::toDramaVO)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期25小时
            redisUtils.set(
                    TaskConstants.DRAMA_PAGE,
                    json,
                    Duration.ofHours(25)
            );
            long duration = System.currentTimeMillis() - startTime;
            log.info("成功缓存 {} 条影视信息到 Redis，耗时: {}ms", voList.size(), duration);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("缓存失败，耗时: {}ms", duration, e);
        }
    }

    // 降低执行频率：每5分钟执行一次，错开与上面任务的执行时间
    @Scheduled(cron = "0 * * * * ?")
    public void cacheLatestDrama() {
        long startTime = System.currentTimeMillis();
        try {
            // 1. 查询所有未删除的剧目
            List<Drama> allDramas = dramaMapper.selectAll();

            if (allDramas == null || allDramas.isEmpty()) {
                log.warn("未查到任何剧目数据，跳过缓存");
                return;
            }

            // 2. 遍历每一条，单独存入 Redis
            for (Drama drama : allDramas) {
                DramaVO vo = toDramaVO(drama);
                String key = TaskConstants.DRAMA + drama.getId();

                redisUtils.set(key, vo, Duration.ofHours(25));
            }

            log.info("成功将 {} 条剧目信息逐条缓存到 Redis", allDramas.size());

        } catch (Exception e) {
            log.error("全量缓存剧目到 Redis 失败", e);
        }
    }

    public DramaVO toDramaVO(Drama drama) {
        if (drama == null) {
            return null;
        }
        return DramaVO.builder()
                .id(drama.getId())
                .name(drama.getName())
                .filingNum(drama.getFilingNum())
                .prodCompany(drama.getProdCompany())
                .crewDescription(drama.getCrewDescription())
                .dramaDescription(drama.getDramaDescription())
                .cast(drama.getCast())
                .shootLocation(drama.getShootLocation())
                .locationId(drama.getLocationId())
                .service(drama.getService())
                .serviceId(drama.getServiceId())
                .userId(drama.getUserId())
                .createdAt(drama.getCreatedAt())
                .updatedAt(drama.getUpdatedAt())
                .image(drama.getImage())
                .thumbImage(drama.getThumbImage())
                .build();

    }
}
