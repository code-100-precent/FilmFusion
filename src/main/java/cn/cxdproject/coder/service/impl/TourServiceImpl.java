package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateTourDTO;
import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Policy;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.PolicyVO;
import cn.cxdproject.coder.model.vo.TourVO;
import cn.cxdproject.coder.service.PolicyService;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private final ObjectProvider<TourService> tourServiceProvider;

    public TourServiceImpl(TourMapper tourMapper, Cache<String, Object> cache, RedisUtils redisUtils, ObjectProvider<TourService> tourServiceProvider) {
        this.tourMapper = tourMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
        this.tourServiceProvider = tourServiceProvider;
    }

    //生成数据
    @Override
    @Loggable(
            type = LogType.TOUR_CREATE,
            value = "Create tour"
    )
    public TourVO createTourByAdmin(CreateTourDTO createDTO) {

        Tour tour = Tour.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .theme(createDTO.getTheme())
                .food(createDTO.getFood())
                .hotel(createDTO.getHotel())
                .features(createDTO.getFeatures())
                .transport(createDTO.getTransport())
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .locationId(createDTO.getLocationId())
                .build();

        tour.setCreatedAt(LocalDateTime.now());
        tour.setUpdatedAt(LocalDateTime.now());

        this.save(tour);
        return toTourVO(tour);
    }

    //根据id单个数据查询
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
            cache.put(CaffeineConstants.TOUR + tourId, tour);
            return toTourVO(tour);
        }
    }

    //客户端批量查询（游标分页）
    @Override
    @CircuitBreaker(name = "tourGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
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


    //管理端更新数据
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

    //管理员删除数据
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
                .locationId(tour.getLocationId())
                .build();
    }

    //单个数据查询降级接口
    @Override
    public TourVO getByIdFallback(Long id,Throwable e)  {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        TourVO tour = redisUtils.get(TaskConstants.TOUR + id, TourVO.class);
        if (tour != null) {
            return tour;
        }
        return null;
    }

    //客户端批量查询降级接口
    @Override
    public List<TourVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
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

    //管理端降级接口
    @Override
    public Page<TourVO> getTourPageAdmin(Page<Tour> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        // 获取当前页的数据和总记录数
        List<Tour> tours = tourMapper.getAdminPage(keyword, offset, size);
        Long total = tourMapper.getTotal(keyword);

        List<TourVO> voList =tours.stream()
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
            CompletableFuture<TourVO> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return tourServiceProvider.getObject()
                                    .getTourById(tourId);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return future.get(Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getByIdFallback(tourId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TourVO> getTourPageWithTimeout(Long lastId, int size, String keyword) {
        try {
            CompletableFuture<List<TourVO>> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return tourServiceProvider.getObject()
                                    .getTourPage(lastId, size, keyword);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return future.get(Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getPageFallback(lastId, size, keyword, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
