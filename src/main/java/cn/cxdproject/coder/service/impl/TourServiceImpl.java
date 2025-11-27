package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateTourDTO;
import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.model.vo.TourVO;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.cxdproject.coder.model.entity.Tour;
import cn.cxdproject.coder.mapper.TourMapper;
import cn.cxdproject.coder.service.TourService;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.NOT_FOUND;

/**
 * Tour 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class TourServiceImpl extends ServiceImpl<TourMapper, Tour> implements TourService {

    private final TourMapper tourMapper;
    private final Cache<String, Object> cache;
    private final RedisUtils redisUtils;

    public TourServiceImpl(TourMapper tourMapper, Cache<String, Object> cache, RedisUtils redisUtils) {
        this.tourMapper = tourMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
    }

    @Override
    public TourVO createTourByAdmin(CreateTourDTO createDTO) {
        if(createDTO.getCover()==null){
            createDTO.setCover(Constants.DEFAULT_COVER);
        }

        Tour tour = Tour.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .theme(createDTO.getTheme())
                .cover(createDTO.getCover())
                .food(createDTO.getFood())
                .hotel(createDTO.getHotel())
                .features(createDTO.getFeatures())
                .transport(createDTO.getFeatures())
                .image(createDTO.getImage())
                .thumbCover(createDTO.getThumbCover())
                .thumbImage(createDTO.getThumbImage())
                .build();

        this.save(tour);
        return toTourVO(tour);
    }

    @Override
    @CircuitBreaker(name = "tourGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public TourVO getTourById(Long tourId) {
        Object store = cache.asMap().get(CaffeineConstants.TOUR + tourId);
        if (store != null) {
            return toTourVO((Tour) store);
        } else {
            Tour tour = this.getById(tourId);
            if (tour == null || Boolean.TRUE.equals(tour.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.TOUR + tourId, tour);
            return toTourVO(tour);
        }
    }

    @Override
    @CircuitBreaker(name = "tourGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public Page<TourVO> getTourPage(Page<Tour> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;


        List<Long> ids = tourMapper.getPageTourIds(keyword, offset, size);
        long total = tourMapper.countByKeyword(keyword);

        if (ids.isEmpty()) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 1. 批量从 Redis 获取缓存
        List<String> keys = ids.stream().map(id -> RedisKeyConstants.TOUR + id).collect(Collectors.toList());
        List<String> cachedJsons = redisUtils.multiGet(keys);

        // 2. 构建Drama列表：优先用缓存，缺失的记录 ID
        List<Tour> tours = new ArrayList<>(Collections.nCopies(ids.size(), null));
        List<Long> missingIds = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            String json = cachedJsons.get(i);
            if (json != null) {
                try {
                    tours.set(i, JsonUtils.fromJson(json, Tour.class));
                } catch (Exception e) {
                    missingIds.add(ids.get(i));
                }
            } else {
                missingIds.add(ids.get(i));
            }
        }

            if (!missingIds.isEmpty()) {
                List<Tour> dbTours = tourMapper.selectBatchIds(missingIds);
                Map<Long, Tour> dbMap = dbTours.stream()
                        .peek(tour -> {
                            // 回填 Redis 缓存
                            redisUtils.set(
                                    RedisKeyConstants.TOUR + tour.getId(),
                                    JsonUtils.toJson(tour),
                                    Duration.ofMinutes(30)
                            );
                            cache.put(CaffeineConstants.TOUR + tour.getId(), tour);
                        })
                        .collect(Collectors.toMap(Tour::getId, a -> a));

                // 填回原位置
                for (int i = 0; i < ids.size(); i++) {
                    if (tours.get(i) == null) {
                        tours.set(i, dbMap.get(ids.get(i)));
                    }
                }
            }


        List<TourVO> voList = tours.stream()
                .map(this::toTourVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Page<TourVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);

    }


    @Override
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    public TourVO updateTourByAdmin(Long tourId, UpdateTourDTO updateDTO) {
        Tour tour = this.getById(tourId);
        if (tour == null || Boolean.TRUE.equals(tour.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getName() != null) tour.setName(updateDTO.getName());
        if (updateDTO.getDescription() != null) tour.setDescription(updateDTO.getDescription());
        if (updateDTO.getTheme() != null) tour.setTheme(updateDTO.getTheme());
        if (updateDTO.getFeatures() != null) tour.setFeatures(updateDTO.getFeatures());
        if (updateDTO.getCover() != null) tour.setCover(updateDTO.getCover());
        if (updateDTO.getTransport() != null) tour.setTransport(updateDTO.getTransport());
        if (updateDTO.getHotel() != null) tour.setHotel(updateDTO.getHotel());
        if (updateDTO.getFood() != null) tour.setFood(updateDTO.getFood());
        if (updateDTO.getImage() != null) tour.setImage(updateDTO.getImage());
        if (updateDTO.getThumbCover() != null) tour.setThumbCover(updateDTO.getThumbCover());
        if (updateDTO.getThumbImage() != null) tour.setThumbImage(updateDTO.getThumbImage());


        tour.setUpdatedAt(LocalDateTime.now());
        cache.asMap().put(CaffeineConstants.TOUR + tourId, tour);
        this.updateById(tour);
        return toTourVO(tour);
    }

    @Override
    @Bulkhead(name = "delete", type = Bulkhead.Type.SEMAPHORE)
    public void deleteTourByAdmin(Long tourId) {
        boolean updated = tourMapper.update(null,
                Wrappers.<Tour>lambdaUpdate()
                        .set(Tour::getDeleted, true)
                        .eq(Tour::getId, tourId)
                        .eq(Tour::getDeleted, false)
        ) > 0;

        if (!updated) {
            Tour tour = this.getById(tourId);
            if (tour == null || Boolean.TRUE.equals(tour.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.TOUR+tourId);

    }

    @Override
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

    @Override
    public TourVO getByIdFallback(Long id,Throwable e) {
        Object store;
        store = cache.getIfPresent(CaffeineConstants.TOUR + id);
        if (store != null) {
            return toTourVO((Tour) store);
        }
        store = redisUtils.get(RedisKeyConstants.TOUR+id);
        if (store != null) {
            Tour tour = JsonUtils.fromJson((String) store, Tour.class);
            return toTourVO(tour);
        }
        return null;
    }

    @Override
    public Page<TourVO> getPageFallback(Page<Tour> page, String keyword, Throwable e) {
        try {
            String json = (String) redisUtils.get(TaskConstants.TOUR);
            if (json == null || json.isEmpty()) {
                return new Page<TourVO>()
                        .setCurrent(page.getCurrent())
                        .setSize(page.getSize())
                        .setTotal(0)
                        .setRecords(Collections.emptyList());
            }

            TourVO[] array = JsonUtils.fromJson(json, TourVO[].class);
            List<TourVO> list = array != null ? Arrays.asList(array) : Collections.emptyList();

            long total = list.size();


            return new Page<TourVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(total)
                    .setRecords(new ArrayList<>(list));
        } catch (Exception ex) {
            log.error("getPageFallback error", ex);
            return new Page<TourVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(0)
                    .setRecords(Collections.emptyList());
        }
    }
}
