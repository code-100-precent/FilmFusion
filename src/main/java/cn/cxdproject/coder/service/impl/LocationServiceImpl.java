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
        if(createDTO.getCover()==null){
            createDTO.setCover(Constants.DEFAULT_COVER);
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
                .cover(createDTO.getCover())
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
    @CircuitBreaker(name = "locationGetPage", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public Page<LocationVO> getLocationPage(Page<Location> page, String keyword) {

        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Long> ids = locationMapper.getPageLocationIds(keyword, offset, size);
        long total = locationMapper.countByKeyword(keyword);

        if (ids.isEmpty()) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 1. 批量从 Redis 获取缓存
        List<String> keys = ids.stream().map(id -> RedisKeyConstants.LOCATION + id).collect(Collectors.toList());
        List<String> cachedJsons = redisUtils.multiGet(keys);

        // 2. 构建Location列表：优先用缓存，缺失的记录 ID
        List<Location> locations = new ArrayList<>(Collections.nCopies(ids.size(), null));
        List<Long> missingIds = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            String json = cachedJsons.get(i);
            if (json != null) {
                try {
                    locations.set(i, JsonUtils.fromJson(json, Location.class));
                } catch (Exception e) {
                    missingIds.add(ids.get(i));
                }
            } else {
                missingIds.add(ids.get(i));
            }
        }

        if (!missingIds.isEmpty()) {
            List<Location> dbArticles = locationMapper.selectBatchIds(missingIds);
            Map<Long, Location> dbMap = dbArticles.stream()
                    .peek(location -> {
                        // 回填 Redis 缓存
                        redisUtils.set(
                                RedisKeyConstants.LOCATION + location.getId(),
                                JsonUtils.toJson(location),
                                Duration.ofMinutes(30)
                        );
                        cache.put(CaffeineConstants.LOCATION+location.getId(),location);
                    })
                    .collect(Collectors.toMap(Location::getId, a -> a));

            // 填回原位置
            for (int i = 0; i < ids.size(); i++) {
                if (locations.get(i) == null) {
                    locations.set(i, dbMap.get(ids.get(i)));
                }
            }
        }

        List<LocationVO> voList = locations.stream()
                .map(this::toLocationVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Page<LocationVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);

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
        if(updateDTO.getCover() != null) location.setCover(updateDTO.getCover());
        if(updateDTO.getImage() != null) location.setImage(updateDTO.getImage());

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
               .cover(location.getCover())
               .createdAt(location.getCreatedAt())
               .updatedAt(location.getUpdatedAt())
               .image(location.getImage())
               .build();
    }

    @Override
    public LocationVO getByIdFallback(Long id) {
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
    public Page<LocationVO> getPageFallback(Page<Location> page, String keyword, Throwable e) {
        try {
            String json = (String) redisUtils.get(TaskConstants.LOCATION);
            if (json == null || json.isEmpty()) {
                return new Page<LocationVO>()
                        .setCurrent(page.getCurrent())
                        .setSize(page.getSize())
                        .setTotal(0)
                        .setRecords(Collections.emptyList());
            }

            LocationVO[] array = JsonUtils.fromJson(json, LocationVO[].class);
            List<LocationVO> list = array != null ? Arrays.asList(array) : Collections.emptyList();

            long total = list.size();

            return new Page<LocationVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(total)
                    .setRecords(new ArrayList<>(list));
        } catch (Exception ex) {
            log.error("getPageFallback error", ex);
            return new Page<LocationVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(0)
                    .setRecords(Collections.emptyList());
        }
    }
}
