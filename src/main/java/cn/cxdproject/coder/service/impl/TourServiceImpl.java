package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.exception.SystemException;
import cn.cxdproject.coder.model.dto.CreateTourDTO;
import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Day;
import cn.cxdproject.coder.model.entity.Tour;
import cn.cxdproject.coder.model.vo.DayVO;
import cn.cxdproject.coder.model.vo.TourVO;
import cn.cxdproject.coder.mapper.TourMapper;
import cn.cxdproject.coder.service.DayService;
import cn.cxdproject.coder.service.TourService;
import cn.cxdproject.coder.utils.AsyncTimeoutUtils;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Tour 服务实现类
 * @author Hibiscus-code-generate
 */
@Slf4j
@Service
public class TourServiceImpl extends ServiceImpl<TourMapper, Tour> implements TourService {

    private final TourMapper tourMapper;
    private final Cache<String, Object> cache;
    private final RedisUtils redisUtils;
    private final ObjectProvider<TourService> tourServiceProvider;
    private final DayService dayService;

    public TourServiceImpl(
            TourMapper tourMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils,
            ObjectProvider<TourService> tourServiceProvider,
            DayService dayService
    ) {
        this.tourMapper = tourMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
        this.tourServiceProvider = tourServiceProvider;
        this.dayService = dayService;
    }

    // 管理员创建线路（带行程和景点），支持事务
    @Override
    @Loggable(
            type = LogType.TOUR_CREATE,
            value = "Create tour"
    )
    @Transactional(rollbackFor = Exception.class)
    public TourVO createTourByAdmin(CreateTourDTO createDTO) {
        // 1. 创建 Tour 主表
        Tour tour = Tour.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .build();

        tour.setCreatedAt(LocalDateTime.now());
        tour.setUpdatedAt(LocalDateTime.now());
        tour.setDeleted(false);

        this.save(tour);

        // 2. 级联创建 Days 和 Attractions
        if (createDTO.getDays() != null && !createDTO.getDays().isEmpty()) {
            dayService.batchCreateDays(tour.getId(), createDTO.getDays());
        }

        // 3. 返回完整的 VO（包含关联数据）
        return toTourVO(tour);
    }

    // 单个线路查询，支持降级熔断，限流，数据进行本地缓存
    @Override
    @CircuitBreaker(name = "tourGetById", fallbackMethod = "getByIdFallback")
    @RateLimiter(name = "tourGet")
    @Bulkhead(name = "tourGet", type = Bulkhead.Type.SEMAPHORE)
    public TourVO getTourById(Long tourId) {
        Object store = cache.getIfPresent(CaffeineConstants.TOUR + tourId);
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

    // 用户线路批量查询，采用游标分页
    @Override
    @CircuitBreaker(name = "tourGetPage", fallbackMethod = "getPageFallback")
    @RateLimiter(name = "tourGet")
    @Bulkhead(name = "tourGet", type = Bulkhead.Type.SEMAPHORE)
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

    // 管理员更新线路（带行程和景点）
    @Override
    @Loggable(
            type = LogType.TOUR_UPDATE,
            value = "Update tour ID: #{#tourId}"
    )
    @Transactional(rollbackFor = Exception.class)
    public TourVO updateTourByAdmin(Long tourId, UpdateTourDTO updateDTO) {
        // 1. 校验是否存在且未被删除
        Tour existing = this.getById(tourId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 2. 执行动态更新（只更新 Tour 主表）
        int updatedRows = tourMapper.updateTour(tourId, updateDTO);

        if (updatedRows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 3. 更新关联的 Days 和 Attractions（如果有传递）
        if (updateDTO.getDays() != null && !updateDTO.getDays().isEmpty()) {
            dayService.batchUpdateDays(tourId, updateDTO.getDays());
        }

        // 4. 重新加载最新数据（保证一致性）
        Tour updatedTour = this.getById(tourId);

        // 5. 更新缓存
        cache.put(CaffeineConstants.TOUR + tourId, updatedTour);

        // 6. 返回 VO
        return toTourVO(updatedTour);
    }

    // 管理员删除线路（级联逻辑删除）
    @Override
    @Loggable(
            type = LogType.TOUR_DELETE,
            value = "Delete tour by ID: #{#tourId}"
    )
    @Transactional(rollbackFor = Exception.class)
    public void deleteTourByAdmin(Long tourId) {
        // 1. 逻辑删除 Tour
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

        // 2. 级联逻辑删除 Days 和 Attractions
        dayService.logicDeleteByTourId(tourId);

        // 3. 删除缓存
        cache.invalidate(CaffeineConstants.TOUR + tourId);
    }

    @Override
    public TourVO toTourVO(Tour tour) {
        if (tour == null) {
            return null;
        }

        // 查询关联的 Days 和 Attractions
        List<Day> days = dayService.listByTourId(tour.getId());
        List<DayVO> dayVOs = days.stream()
                .map(dayService::toDayVO)
                .collect(Collectors.toList());

        return TourVO.builder()
                .id(tour.getId())
                .name(tour.getName())
                .description(tour.getDescription())
                .days(dayVOs)
                .createdAt(tour.getCreatedAt())
                .updatedAt(tour.getUpdatedAt())
                .build();
    }

    // 查询单个线路数据的降级策略（查询缓存的数据）
    @Override
    public TourVO getByIdFallback(Long id, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException
                || e instanceof RequestNotPermitted || e instanceof BulkheadFullException) {
            throw (RuntimeException) e;
        }

        TourVO tour = redisUtils.get(TaskConstants.TOUR + id, TourVO.class);

        if (tour != null) {
            return tour;
        }
        throw new SystemException(SERVICE_UNAVAILABLE.code(), "服务暂时不可用，请稍后重试");
    }

    // 游标查询降级策略
    @Override
    public List<TourVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException
                || e instanceof RequestNotPermitted || e instanceof BulkheadFullException) {
            throw (RuntimeException) e;
        }

        try {
            String json = (String) redisUtils.get(TaskConstants.TOUR_PAGE);
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

    // 管理端分页查询
    @Override
    public Page<TourVO> getTourPageAdmin(Page<Tour> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        // 获取当前页的数据和总记录数
        List<Tour> tours = tourMapper.getAdminPage(keyword, offset, size);
        Long total = tourMapper.getTotal(keyword);

        List<TourVO> voList = tours.stream()
                .map(this::toTourVO)
                .collect(Collectors.toList());

        return new Page<TourVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }

    @Override
    public TourVO getTourByIdWithTimeout(Long tourId) {
        try {
            return AsyncTimeoutUtils.runWithTimeout(
                    () -> tourServiceProvider.getObject().getTourById(tourId),
                    Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getByIdFallback(tourId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TourVO> getTourPageWithTimeout(Long lastId, int size, String keyword) {
        try {
            return AsyncTimeoutUtils.runWithTimeout(
                    () -> tourServiceProvider.getObject().getTourPage(lastId, size, keyword),
                    Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getPageFallback(lastId, size, keyword, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
