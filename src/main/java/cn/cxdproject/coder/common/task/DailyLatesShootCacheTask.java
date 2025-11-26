package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.ShootMapper;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DailyLatesShootCacheTask {
    private final ShootMapper shootMapper;

    private final RedisUtils redisUtils;

    public DailyLatesShootCacheTask(ShootMapper shootMapper,
                                    RedisUtils redisUtils) {
        this.shootMapper = shootMapper;
        this.redisUtils = redisUtils;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatest10Shoots() {
        try {
            // 1. 从数据库查询最新10条
            List<Shoot> latestShoots = shootMapper.selectLatest10();

            if (latestShoots == null || latestShoots.isEmpty()) {
                log.warn("未查到协拍服务，跳过缓存");
                return;
            }

            // 2. 转为VO列表
            List<ShootVO> voList = latestShoots.stream()
                    .map(this::toShootVO)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期25小时
            redisUtils.set(
                    TaskConstants.SHOOT,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条协拍服务信息到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

    public ShootVO toShootVO(Shoot shoot) {
        if (shoot == null) {
            return null;
        }
        return ShootVO.builder()
                .id(shoot.getId())
                .name(shoot.getName())
                .description(shoot.getDescription())
                .price(shoot.getPrice())
                .status(shoot.getStatus())
                .address(shoot.getAddress())
                .phone(shoot.getPhone())
                .contactName(shoot.getContactName())
                .userId(shoot.getUserId())
                .cover(shoot.getCover())
                .createdAt(shoot.getCreatedAt())
                .updatedAt(shoot.getUpdatedAt())
                .image(shoot.getImage())
                .build();
    }
}
