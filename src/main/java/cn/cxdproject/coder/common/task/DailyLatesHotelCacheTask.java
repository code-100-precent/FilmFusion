package cn.cxdproject.coder.common.task;


import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.HotelMapper;
import cn.cxdproject.coder.model.entity.Hotel;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.HotelVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
//Hotel的定时任务（用于分页降级时查询的数据）
@Component
@Slf4j
public class DailyLatesHotelCacheTask {

    private final HotelMapper hotelMapper;
    private final RedisUtils redisUtils;

    public DailyLatesHotelCacheTask(HotelMapper hotelMapper, RedisUtils redisUtils) {
        this.hotelMapper = hotelMapper;
        this.redisUtils = redisUtils;
    }


    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestHotelPage() {
        try {
            // 1. 从数据库查询最新10条
            List<Hotel> latestHotels = hotelMapper.selectLatest10();

            if (latestHotels == null || latestHotels.isEmpty()) {
                log.warn("未查到旅店数据，跳过缓存");
                return;
            }

            // 2. 转为 VO 列表
            List<HotelVO> voList = latestHotels.stream()
                    .map(this::toHotelVO)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期25小时
            redisUtils.set(
                    TaskConstants.HOTEL_PAGE,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条影视信息到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatestHotel() {
        try {
            // 1. 从数据库查询最新1条酒店数据
            Hotel latestHotel = hotelMapper.selectLatestOne();

            if (latestHotel == null) {
                log.warn("未查到任何酒店数据，跳过缓存");
                return;
            }

            // 2. 转为 VO
            HotelVO vo = toHotelVO(latestHotel);

            // 3. 序列化为 JSON（单个对象）
            String json = JsonUtils.toJson(vo);

            // 4. 写入 Redis，有效期 25 小时
            redisUtils.set(
                    TaskConstants.HOTEL,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存最新1条酒店信息到 Redis");

        } catch (Exception e) {
            log.error("缓存最新酒店信息失败", e);
        }
    }

    public HotelVO toHotelVO(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        return HotelVO.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .managerName(hotel.getManagerName())
                .managerPhone(hotel.getManagerPhone())
                .address(hotel.getAddress())
                .image(hotel.getImage())
                .updatedAt(hotel.getUpdatedAt())
                .createdAt(hotel.getCreatedAt())
                .thumbImage(hotel.getThumbImage())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .build();
    }

}
