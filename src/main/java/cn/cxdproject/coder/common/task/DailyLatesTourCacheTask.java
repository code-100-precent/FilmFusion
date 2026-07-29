package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.TourMapper;
import cn.cxdproject.coder.model.entity.Tour;
import cn.cxdproject.coder.model.vo.TourVO;
import cn.cxdproject.coder.service.TourService;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tour的定时任务（用于分页降级时查询的数据）
 */
@Component
@Slf4j
public class DailyLatesTourCacheTask {

    private final TourMapper tourMapper;
    private final RedisUtils redisUtils;
    private final TourService tourService;

    public DailyLatesTourCacheTask(TourMapper tourMapper, RedisUtils redisUtils, TourService tourService) {
        this.tourMapper = tourMapper;
        this.redisUtils = redisUtils;
        this.tourService = tourService;
    }

    /**
     * 每日凌晨 2 点刷一次降级缓存（分页数据）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestTourPage() {
        try {
            // 1. 从数据库查询最新10条
            List<Tour> latestTours = tourMapper.selectLatest10();

            if (latestTours == null || latestTours.isEmpty()) {
                log.warn("未查到旅游路线数据，跳过缓存");
                return;
            }

            // 2. 转为VO（包含关联的 Days 和 Attractions）
            List<TourVO> voList = latestTours.stream()
                    .map(tourService::toTourVO)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期25小时
            redisUtils.set(
                    TaskConstants.TOUR_PAGE,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条旅游路线到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存旅游路线分页数据失败", e);
        }
    }

    /**
     * 每日凌晨 2 点刷一次降级缓存（单个数据）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestTourId() {
        try {
            // 1. 查询所有未删除的旅游线路数据
            List<Tour> allTours = tourMapper.selectAll();

            if (allTours == null || allTours.isEmpty()) {
                log.warn("未查到任何旅游线路数据，跳过缓存");
                return;
            }

            // 2. 遍历每一条，单独存入 Redis（包含关联的 Days 和 Attractions）
            for (Tour tour : allTours) {
                TourVO vo = tourService.toTourVO(tour);
                String key = TaskConstants.TOUR + tour.getId();

                redisUtils.set(key, vo, Duration.ofHours(25));
            }

            log.info("成功将 {} 条旅游线路信息逐条缓存到 Redis", allTours.size());

        } catch (Exception e) {
            log.error("全量缓存旅游线路到 Redis 失败", e);
        }
    }
}

