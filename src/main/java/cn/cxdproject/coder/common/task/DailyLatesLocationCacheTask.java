package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DailyLatesLocationCacheTask {
    private final LocationMapper locationMapper;

    private final RedisTemplate<String, String> redisTemplate;

    public DailyLatesLocationCacheTask(LocationMapper locationMapper, RedisTemplate<String, String> redisTemplate) {
        this.locationMapper = locationMapper;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatest10Articles() {
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
                    .filter(vo -> vo != null)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期 25 小时
            redisTemplate.opsForValue().set(
                    TaskConstants.LOCATION,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条地址到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
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
                .cover(location.getCover())
                .createdAt(location.getCreatedAt())
                .updatedAt(location.getUpdatedAt())
                .image(location.getImage())
                .build();
    }
}
