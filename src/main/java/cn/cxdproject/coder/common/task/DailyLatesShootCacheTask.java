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
//Shoot的定时任务（用于分页降级时查询的数据）
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
    public void cacheLatestShootPage() {
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
                    TaskConstants.SHOOT_PAGE,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条协拍服务信息到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestShootId() {
        try {
            // 1. 从数据库查询最新1条
            Shoot latestShoot = shootMapper.selectLatestOne();

            if (latestShoot == null) {
                log.warn("未查到任何 Shoot 数据，跳过缓存");
                return;
            }

            // 2. 转为 VO
            ShootVO vo = toShootVO(latestShoot);

            // 3. 序列化（单个对象）
            String json = JsonUtils.toJson(vo);

            // 4. 写入 Redis，有效期25小时
            redisUtils.set(
                    TaskConstants.SHOOT,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存最新1条 Shoot 到 Redis");

        } catch (Exception e) {
            log.error("缓存最新 Shoot 失败", e);
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
                .createdAt(shoot.getCreatedAt())
                .updatedAt(shoot.getUpdatedAt())
                .image(shoot.getImage())
                .thumbImage(shoot.getThumbImage())
                .build();
    }
}
