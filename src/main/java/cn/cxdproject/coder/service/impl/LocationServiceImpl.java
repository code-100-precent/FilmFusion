package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.HotelVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.service.LocationService;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Location 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {

    private final LocationMapper locationMapper;
    private final Cache<String, Object> cache;
    public  final RedisUtils redisUtils;
    private final ObjectProvider<LocationService> locationServiceProvider;

    public LocationServiceImpl(
            LocationMapper locationMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils,
            ObjectProvider<LocationService> locationServiceProvider) {
        this.locationMapper = locationMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
        this.locationServiceProvider = locationServiceProvider;
    }

    //创建数据（仅限管理员）
    @Override
    @Loggable(
            type = LogType.LOCATION_CREATE,
            value = "Create location"
    )
    public LocationVO createLocationByAdmin(Long userId, CreateLocationDTO createDTO) {

        Location location = Location.builder()
                .name(createDTO.getName())
                .type(createDTO.getType())
                .status(createDTO.getStatus())
                .locationDescription(createDTO.getLocationDescription())
                .locationPrincipalPhone(createDTO.getLocationPrincipalPhone())
                .locationPrincipalName(createDTO.getLocationPrincipalName())
                .govPrincipalPhone(createDTO.getGovPrincipalPhone())
                .govPrincipalName(createDTO.getGovPrincipalName())
                .address(createDTO.getAddress())
                .price(createDTO.getPrice())
                .userId(userId)
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .longitude(createDTO.getLongitude())
                .latitude(createDTO.getLatitude())
                .dramaId(createDTO.getDramaId())
                .build();

        location.setCreatedAt(LocalDateTime.now());
        location.setUpdatedAt(LocalDateTime.now());

        this.save(location);
        return toLocationVO(location);
    }

    //根据id查询
    @Override
    @CircuitBreaker(name = "locationGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public LocationVO getLocationById(Long locationId) {
        Object store = cache.asMap().get(CaffeineConstants.LOCATION + locationId);
        if (store != null) {
            return toLocationVO((Location) store);
        } else {
            Location location = this.getById(locationId);
            if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.put(CaffeineConstants.LOCATION + locationId, location);
            return toLocationVO(location);
        }
    }

    //客户端批量查询（游标分页）
    @Override
    @CircuitBreaker(name = "locationGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public List<LocationVO> getLocationPage(Long lastId, int size, String keyword) {
        List<Long> ids = locationMapper.selectIds(lastId, size, keyword);

        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Location> locations = locationMapper.selectBatchIds(ids);

        Map<Long, Location> locationMap = locations.stream()
                .collect(Collectors.toMap(Location::getId, a -> a));

        return ids.stream()
                .map(locationMap::get)
                .filter(Objects::nonNull)
                .map(this::toLocationVO)
                .collect(Collectors.toList());
    }



    //更新数据（仅限管理员）
    @Override
    @Loggable(
            type = LogType.LOCATION_UPDATE,
            value = "Update location ID: #{#locationId}"
    )
    public LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO) {
        // 1. 校验是否存在且未删除
        Location existing = this.getById(locationId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 2. 执行动态更新
        int updatedRows = locationMapper.updateLocation(locationId, updateDTO);

        // 3. 若无记录被更新（可能已被删除）
        if (updatedRows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 4. 重新加载最新数据（保证缓存和返回的是最新状态）
        Location updatedLocation = this.getById(locationId);

        // 5. 更新缓存
        cache.put(CaffeineConstants.LOCATION + locationId, updatedLocation);

        // 6. 返回 VO
        return toLocationVO(updatedLocation);
    }

    //删除数据（仅限管理员）
    @Override
    @Loggable(
            type = LogType.LOCATION_DELETE,
            value = "Delete location by ID: #{#locationId}"
    )
    public void deleteLocationByAdmin(Long locationId) {
        boolean updated = locationMapper.update(null,
                Wrappers.<Location>lambdaUpdate()
                        .set(Location::getDeleted, true)
                        .eq(Location::getId, locationId)
                        .eq(Location::getDeleted, false)
        ) > 0;

        if (!updated) {
            Location location = this.getById(locationId);
            if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.LOCATION+locationId);
    }


    @Override
    public LocationVO toLocationVO(Location location) {
        if (location == null) {
            return null;
        }
       return LocationVO.builder()
               .id(location.getId())
               .name(location.getName())
               .type(location.getType())
               .status(location.getStatus())
               .locationDescription(location.getLocationDescription())
               .locationPrincipalName(location.getLocationPrincipalName())
               .locationPrincipalPhone(location.getLocationPrincipalPhone())
               .govPrincipalName(location.getGovPrincipalName())
               .govPrincipalPhone(location.getGovPrincipalPhone())
               .address(location.getAddress())
               .price(location.getPrice())
               .userId(location.getUserId())
               .createdAt(location.getCreatedAt())
               .updatedAt(location.getUpdatedAt())
               .image(location.getImage())
               .thumbImage(location.getThumbImage())
               .longitude(location.getLongitude())
               .latitude(location.getLatitude())
               .dramaId(location.getDramaId())
               .build();
    }

    //单个id查询降级接口
    @Override
    public LocationVO getByIdFallback(Long id,Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        Object store = cache.getIfPresent(CaffeineConstants.LOCATION + id);
        if (store != null) {
            return toLocationVO((Location) store);
        }
        return null;
    }

    //客户端批量查询降级接口
    @Override
    public List<LocationVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        try {
            // 从 Redis 获取缓存的全量文章（假设是 ArticleVO[] 的 JSON）
            String json = (String) redisUtils.get(TaskConstants.LOCATION);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            LocationVO[] array = JsonUtils.fromJson(json, LocationVO[].class);
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

    //管理端分页接口
    @Override
    public Page<LocationVO> getLocationPageAdmin(Page<Location> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        // 获取当前页的数据和总记录数
        List<Location> locations = locationMapper.getAdminPage(keyword, offset, size);
        Long total = locationMapper.getTotal(keyword);

        List<LocationVO> voList = locations.stream()
                .map(this::toLocationVO)
                .collect(Collectors.toList());

        return new Page<LocationVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }

    @Override
    public List<LocationVO> getLocationPageWithTimeout(Long lastId, int size, String keyword) {
        try {
            CompletableFuture<List<LocationVO>> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return locationServiceProvider.getObject()
                                    .getLocationPage(lastId, size, keyword);
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

    @Override
    public LocationVO getLocationByIdWithTimeout(Long locationId) {
        try {
            CompletableFuture<LocationVO> future =
                    CompletableFuture.supplyAsync(() -> locationServiceProvider.getObject().getLocationById(locationId));
            return future.get(Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getByIdFallback(locationId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
