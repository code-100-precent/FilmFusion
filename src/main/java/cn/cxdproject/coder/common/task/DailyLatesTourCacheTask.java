package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.TourMapper;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Tour;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.TourVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DailyLatesTourCacheTask {

    private final TourMapper tourMapper;
    private final RedisUtils redisUtils;

    public DailyLatesTourCacheTask(TourMapper tourMapper, RedisUtils redisUtils) {
        this.tourMapper = tourMapper;
        this.redisUtils = redisUtils;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatest10Tours() {
        try {
            // 1. 从数据库查询最新10条
            List<Tour> latestTours = tourMapper.selectLatest10();

            if (latestTours == null || latestTours.isEmpty()) {
                log.warn("未查到文章数据，跳过缓存");
                return;
            }

            // 2. 转为VO
            List<TourVO> voList = latestTours.stream()
                    .map(this::toTourVO)
                    .collect(Collectors.toList());


            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期25小时
            redisUtils.set(
                    TaskConstants.ARTICLE,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条文章到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

    public TourVO toTourVO(Tour tour) {
        if (tour == null) {
            return null;
        }
        return TourVO.builder()
                .id(tour.getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .theme(tour.getTheme())
                .features(tour.getFeatures())
                .cover(tour.getCover())
                .transport(tour.getTransport())
                .hotel(tour.getHotel())
                .food(tour.getFood())
                .image(tour.getImage())
                .createdAt(tour.getCreatedAt())
                .updatedAt(tour.getUpdatedAt())
                .thumbCover(tour.getThumbCover())
                .thumbImage(tour.getThumbImage())
                .build();
    }
}
