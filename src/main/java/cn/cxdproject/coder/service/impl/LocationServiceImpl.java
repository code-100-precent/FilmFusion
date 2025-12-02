package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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

    public LocationServiceImpl(
            LocationMapper locationMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils) {
        this.locationMapper = locationMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
    }

    @Override
    @Bulkhead(name = "add", type = Bulkhead.Type.SEMAPHORE)
    public LocationVO createLocationByAdmin(Long userId, CreateLocationDTO createDTO) {
        if (createDTO.getImage() == null) {
            createDTO.setImage(Constants.DEFAULT_COVER);
            createDTO.setThumbImage(Constants.DEFAULT_THUMB_COVER);
        }

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
                .build();

        this.save(location);
        return toLocationVO(location);
    }

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
            cache.asMap().put(CaffeineConstants.LOCATION + locationId, location);
            return toLocationVO(location);
        }
    }

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


    @Override
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    public LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getName() != null) location.setName(updateDTO.getName());
        if (updateDTO.getType() != null) location.setType(updateDTO.getType());
        if (updateDTO.getStatus() != null) location.setStatus(updateDTO.getStatus());
        if (updateDTO.getLocationDescription() != null) location.setLocationDescription(updateDTO.getLocationDescription());
        if (updateDTO.getLocationPrincipalName() != null) location.setLocationPrincipalName(updateDTO.getLocationPrincipalName());
        if (updateDTO.getLocationPrincipalPhone() != null) location.setLocationPrincipalPhone(updateDTO.getLocationPrincipalPhone());
        if (updateDTO.getGovPrincipalName() != null) location.setGovPrincipalName(updateDTO.getGovPrincipalName());
        if (updateDTO.getGovPrincipalPhone() != null) location.setGovPrincipalPhone(updateDTO.getGovPrincipalPhone());
        if (updateDTO.getAddress() != null) location.setAddress(updateDTO.getAddress());
        if (updateDTO.getPrice() != null) location.setPrice(updateDTO.getPrice());
        if(updateDTO.getImage() != null) location.setImage(updateDTO.getImage());
        if(updateDTO.getThumbImage() != null) location.setThumbImage(updateDTO.getThumbImage());
        if(updateDTO.getLongitude() != null) location.setLongitude(updateDTO.getLongitude());
        if(updateDTO.getLatitude() != null) location.setLatitude(updateDTO.getLatitude());

        cache.asMap().put(CaffeineConstants.LOCATION + locationId, location);
        this.updateById(location);
        return toLocationVO(location);
    }

    @Override
    @Bulkhead(name = "delete", type = Bulkhead.Type.SEMAPHORE)
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
               .build();
    }

    @Override
    public LocationVO getByIdFallback(Long id,Throwable e) {
        Object store;
        store = cache.getIfPresent(CaffeineConstants.LOCATION + id);
        if (store != null) {
            return toLocationVO((Location) store);
        }
        store = redisUtils.get(RedisKeyConstants.LOCATION+id);
        if (store != null) {
            Location location = JsonUtils.fromJson((String) store, Location.class);
            return toLocationVO(location);
        }
        return null;
    }

    @Override
    public List<LocationVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

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

    @Override
    public Page<LocationVO> getLocationPageAdmin(Page<Location> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Location> locations = locationMapper.getAdminPage(keyword, offset, size);

        List<LocationVO> voList = locations.stream()
                .map(this::toLocationVO)
                .collect(Collectors.toList());

        return new Page<LocationVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }
}
