package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateTourDTO;
import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
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
    @Loggable(
            type = LogType.TOUR_CREATE,
            value = "Create tour"
    )
    public TourVO createTourByAdmin(CreateTourDTO createDTO) {
        if (createDTO.getImage() == null) {
            createDTO.setImage(Constants.DEFAULT_COVER);
            createDTO.setThumbImage(Constants.DEFAULT_THUMB_COVER);
        }

        Tour tour = Tour.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .theme(createDTO.getTheme())
                .food(createDTO.getFood())
                .hotel(createDTO.getHotel())
                .features(createDTO.getFeatures())
                .transport(createDTO.getFeatures())
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .latitude(createDTO.getLatitude())
                .longitude(createDTO.getLongitude())
                .longitude(createDTO.getLongitude())
                .locationId(createDTO.getLocationId())
                .dramaId(createDTO.getDramaId())
                .build();

        this.save(tour);
        return toTourVO(tour);
    }

    @Override
    @CircuitBreaker(name = "tourGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    @Loggable(
            type = LogType.TOUR_GET,
            value = "Get tour by ID: #{#tourId}"
    )
    public TourVO getTourById(Long tourId) {
        Object store = cache.asMap().get(CaffeineConstants.TOUR + tourId);
        if (store != null) {
            return toTourVO((Tour) store);
        } else {
            Tour tour = this.getById(tourId);
            if (tour == null || Boolean.TRUE.equals(tour.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.put(CaffeineConstants.TOUR + tourId, tour);
            return toTourVO(tour);
        }
    }

    @Override
    @CircuitBreaker(name = "tourGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    @Loggable(
            type = LogType.TOUR_USER_GET_PAGE,
            value = "User get tour"
    )
    public List<TourVO> getTourPage(Long lastId, int size, String keyword) {
        List<Long> ids = tourMapper.selectIds(lastId, size, keyword);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Tour> tours = tourMapper.selectBatchIds(ids);

        Map<Long, Tour> tourMap = tours.stream()
                .collect(Collectors.toMap(Tour::getId, a -> a));

        return ids.stream()
                .map(tourMap::get)
                .filter(Objects::nonNull)
                .map(this::toTourVO)
                .collect(Collectors.toList());
    }


    @Override
    @Loggable(
            type = LogType.TOUR_UPDATE,
            value = "Update tour ID: #{#tourId}"
    )
    public TourVO updateTourByAdmin(Long tourId, UpdateTourDTO updateDTO) {
        // 1. 校验是否存在且未被删除
        Tour existing = this.getById(tourId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 3. 执行动态更新
        int updatedRows = tourMapper.updateTour(tourId, updateDTO);

        // 4. 若无记录被更新（可能已被删除或并发修改）
        if (updatedRows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 5. 重新加载最新数据（保证一致性）
        Tour updatedTour = this.getById(tourId);

        // 6. 更新缓存（注意：缓存的是对象，不是旧引用）
        cache.put(CaffeineConstants.TOUR + tourId, updatedTour);

        // 7. 返回 VO
        return toTourVO(updatedTour);
    }

    @Override
    @Loggable(
            type = LogType.TOUR_DELETE,
            value = "Delete tour by ID: #{#tourId}"
    )
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
                .transport(tour.getTransport())
                .hotel(tour.getHotel())
                .food(tour.getFood())
                .image(tour.getImage())
                .createdAt(tour.getCreatedAt())
                .updatedAt(tour.getUpdatedAt())
                .thumbImage(tour.getThumbImage())
                .longitude(tour.getLongitude())
                .latitude(tour.getLatitude())
                .locationId(tour.getLocationId())
                .dramaId(tour.getDramaId())
                .build();
    }

    @Override
    public TourVO getByIdFallback(Long id,Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        Object store = cache.getIfPresent(CaffeineConstants.TOUR + id);
        if (store != null) {
            return toTourVO((Tour) store);
        }
        return null;
    }

    @Override
    public List<TourVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        try {

            String json = (String) redisUtils.get(TaskConstants.TOUR);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            TourVO[] array = JsonUtils.fromJson(json, TourVO[].class);
            if (array == null || array.length == 0) {
                return Collections.emptyList();
            }

            int take = Math.min(size, array.length);
            return new ArrayList<>(Arrays.asList(array).subList(0, take));

        } catch (Exception ex) {
            log.error("Fallback failed", ex);
            return Collections.emptyList();
        }
    }

    @Override
    @Loggable(
            type = LogType.TOUR_ADMIN_GET_PAGE,
            value = "Admin get tour Page"
    )
    public Page<TourVO> getTourPageAdmin(Page<Tour> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Tour> tours = tourMapper.getAdminPage(keyword, offset, size);
        long total = tourMapper.countAdminPage(keyword);

        List<TourVO> voList = tours.stream()
                .map(this::toTourVO)
                .collect(Collectors.toList());

        return new Page<TourVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);
    }
}
