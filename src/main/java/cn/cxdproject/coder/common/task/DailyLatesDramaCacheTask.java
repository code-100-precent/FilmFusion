package cn.cxdproject.coder.common.task;

import cn.cxdproject.coder.common.constants.TaskConstants;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DailyLatesDramaCacheTask {
    private final DramaMapper dramaMapper;

    private final RedisTemplate<String, String> redisTemplate;

    public DailyLatesDramaCacheTask(DramaMapper dramaMapper, RedisTemplate<String, String> redisTemplate) {
        this.dramaMapper = dramaMapper;
        this.redisTemplate = redisTemplate;
    }


    @Scheduled(cron = "0 0 2 * * ?")
    public void cacheLatest10Articles() {
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
                    .filter(vo -> vo != null)
                    .collect(Collectors.toList());

            // 3. 序列化
            String json = JsonUtils.toJson(voList);

            // 4. 写入 Redis，有效期25小时
            redisTemplate.opsForValue().set(
                    TaskConstants.DRAMA,
                    json,
                    Duration.ofHours(25)
            );
            log.info("成功缓存 {} 条影视信息到 Redis", voList.size());
        } catch (Exception e) {
            log.error("缓存失败", e);
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
                .cover(drama.getCover())
                .createdAt(drama.getCreatedAt())
                .updatedAt(drama.getUpdatedAt())
                .image(drama.getImage())
                .build();

    }
}
