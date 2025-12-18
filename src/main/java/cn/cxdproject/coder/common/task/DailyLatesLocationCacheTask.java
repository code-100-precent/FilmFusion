package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
//Location的定时任务（用于分页降级时查询的数据）
@Component
@Slf4j
public class DailyLatesLocationCacheTask {
    private final LocationMapper locationMapper;
    private final RedisUtils redisUtils;

    public DailyLatesLocationCacheTask(LocationMapper locationMapper,
                                       RedisUtils redisUtils) {
        this.locationMapper = locationMapper;
        this.redisUtils = redisUtils;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestLocationPage() {
        try {
            // 1. 从数据库查询最新10条
            List<Location> latestLocations = locationMapper.selectLatest10();

            if (latestLocations == null || latestLocations.isEmpty()) {
                log.warn("未查到地址数据，跳过缓存");
                return;
            }

            // 2. 转为VO列表
            List<LocationVO> voList = latestLocations.stream()
                    .map(this::toLocationVO)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期 25 小时
            redisUtils.set(
                    TaskConstants.LOCATION_PAGE,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条地址到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestLocation() {
        try {
            // 1. 查询所有未删除的地点
            List<Location> allLocations = locationMapper.selectAll();

            if (allLocations == null || allLocations.isEmpty()) {
                log.warn("未查到任何地点数据，跳过缓存");
                return;
            }

            // 2. 遍历每一条，单独存入 Redis
            for (Location location : allLocations) {
                LocationVO vo = toLocationVO(location);
                String key = TaskConstants.LOCATION + location.getId();

                redisUtils.set(key, vo, Duration.ofHours(25));
            }

            log.info("成功将 {} 条地点信息逐条缓存到 Redis", allLocations.size());

        } catch (Exception e) {
            log.error("全量缓存地点到 Redis 失败", e);
        }
    }

    public LocationVO toLocationVO(Location location) {
        if (location == null) {
            return null;
        }
        return LocationVO.builder()
                .id(location.getId())
                .name(location.getName())
                .type(location.getType())
                .status(location.getStatus())
                .locationDescription(location.getLocationDescription())
                .locationPrincipalName(location.getLocationPrincipalName())
                .locationPrincipalPhone(location.getLocationPrincipalPhone())
                .govPrincipalName(location.getGovPrincipalName())
                .govPrincipalPhone(location.getGovPrincipalPhone())
                .address(location.getAddress())
                .price(location.getPrice())
                .userId(location.getUserId())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .image(location.getImage())
                .thumbImage(location.getThumbImage())
                .longitude(location.getLongitude())
                .latitude(location.getLatitude())
                .dramaId(location.getDramaId())
                .build();
    }
}
