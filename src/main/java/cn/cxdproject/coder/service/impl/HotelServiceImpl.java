package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.mapper.HotelMapper;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.CreateHotelDTO;
import cn.cxdproject.coder.model.dto.UpdateHotelDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Hotel;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.HotelVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.service.HotelService;
import cn.cxdproject.coder.utils.JsonUtils;
import cn.cxdproject.coder.utils.RedisUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.NOT_FOUND;

@Slf4j
@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements HotelService {

    private final HotelMapper hotelMapper;
    private final Cache<String, Object> cache;
    public  final RedisUtils redisUtils;

    public HotelServiceImpl(
            HotelMapper hotelMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils) {
        this.hotelMapper = hotelMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
    }


    @Override
    @Bulkhead(name = "add", type = Bulkhead.Type.SEMAPHORE)
    public HotelVO createHotelByAdmin(Long userId, CreateHotelDTO createDTO) {
        if(createDTO.getCover()==null){
            createDTO.setCover(Constants.DEFAULT_COVER);
        }
        Hotel hotel = Hotel.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .address(createDTO.getAddress())
                .managerName(createDTO.getManagerName())
                .managerPhone(createDTO.getManagerPhone())
                .cover(createDTO.getCover())
                .image(createDTO.getImage())
                .userId(userId)
                .build();

        this.save(hotel);
        return toHotelVO(hotel);
    }

    @Override
    @CircuitBreaker(name = "hotelGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public HotelVO getHotelById(Long hotelId) {
        Object store = cache.asMap().get(CaffeineConstants.HOTEL + hotelId);
        if (store != null) {
            return toHotelVO((Hotel) store);
        } else {
            Hotel hotel = this.getById(hotelId);
            if (hotel == null || Boolean.TRUE.equals(hotel.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.HOTEL + hotelId, hotel);
            return toHotelVO(hotel);
        }
    }

    @Override
    @CircuitBreaker(name = "hotelGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public Page<HotelVO> getHotelPage(Page<Hotel> page, String keyword) {

        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Long> ids = hotelMapper.getPageHotelIds(keyword, offset, size);
        long total = hotelMapper.countByKeyword(keyword);

        if (ids.isEmpty()) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 1. 批量从 Redis 获取缓存
        List<String> keys = ids.stream().map(id -> RedisKeyConstants.HOTEL + id).collect(Collectors.toList());
        List<String> cachedJsons = redisUtils.multiGet(keys);

        // 2. 构建Location列表：优先用缓存，缺失的记录 ID
        List<Hotel> locations = new ArrayList<>(Collections.nCopies(ids.size(), null));
        List<Long> missingIds = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            String json = cachedJsons.get(i);
            if (json != null) {
                try {
                    locations.set(i, JsonUtils.fromJson(json, Hotel.class));
                } catch (Exception e) {
                    missingIds.add(ids.get(i));
                }
            } else {
                missingIds.add(ids.get(i));
            }
        }

        if (!missingIds.isEmpty()) {
            List<Hotel> dbArticles = hotelMapper.selectBatchIds(missingIds);
            Map<Long, Hotel> dbMap = dbArticles.stream()
                    .peek(hotel -> {
                        // 回填 Redis 缓存
                        redisUtils.set(
                                RedisKeyConstants.HOTEL + hotel.getId(),
                                JsonUtils.toJson(hotel),
                                Duration.ofMinutes(30)
                        );
                        cache.put(CaffeineConstants.HOTEL+hotel.getId(),hotel);
                    })
                    .collect(Collectors.toMap(Hotel::getId, a -> a));

            // 填回原位置
            for (int i = 0; i < ids.size(); i++) {
                if (locations.get(i) == null) {
                    locations.set(i, dbMap.get(ids.get(i)));
                }
            }
        }

        List<HotelVO> voList = locations.stream()
                .map(this::toHotelVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Page<HotelVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);

    }


    @Override
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    public HotelVO updateHotelByAdmin(Long hotelId, UpdateHotelDTO updateDTO) {
        Hotel hotel = this.getById(hotelId);
        if (hotel == null || Boolean.TRUE.equals(hotel.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getName() != null) hotel.setName(updateDTO.getName());
        if (updateDTO.getDescription() != null) hotel.setDescription(updateDTO.getDescription());
        if (updateDTO.getAddress() != null) hotel.setAddress(updateDTO.getAddress());
        if (updateDTO.getManagerName() != null) hotel.setManagerName(updateDTO.getManagerName());
        if (updateDTO.getManagerPhone() != null) hotel.setManagerPhone(updateDTO.getManagerPhone());
        if (updateDTO.getCover() != null) hotel.setCover(updateDTO.getCover());
        if (updateDTO.getImage() != null) hotel.setImage(updateDTO.getImage());

        cache.asMap().put(CaffeineConstants.HOTEL + hotelId, hotel);
        this.updateById(hotel);
        return toHotelVO(hotel);
    }


    @Override
    @Bulkhead(name = "delete", type = Bulkhead.Type.SEMAPHORE)
    public void deleteHotelByAdmin(Long hotelId) {
        boolean updated = hotelMapper.update(null,
                Wrappers.<Hotel>lambdaUpdate()
                        .set(Hotel::getDeleted, true)
                        .eq(Hotel::getId, hotelId)
                        .eq(Hotel::getDeleted, false)
        ) > 0;

        if (!updated) {
            Hotel hotel = this.getById(hotelId);
            if (hotel == null || Boolean.TRUE.equals(hotel.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.HOTEL+hotelId);
    }

    @Override
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
                .cover(hotel.getCover())
                .updatedAt(hotel.getUpdatedAt())
                .createdAt(hotel.getCreatedAt())
                .userId(hotel.getUserId())
                .build();
    }

    @Override
    public HotelVO getByIdFallback(Long id) {
        Object store;
        store = cache.getIfPresent(CaffeineConstants.HOTEL + id);
        if (store != null) {
            return toHotelVO((Hotel) store);
        }
        store = redisUtils.get(RedisKeyConstants.HOTEL+id);
        if (store != null) {
            Hotel hotel = JsonUtils.fromJson((String) store, Hotel.class);
            return toHotelVO(hotel);
        }
        return null;
    }

    @Override
    public Page<HotelVO> getPageFallback(Page<Hotel> page, String keyword, Throwable e) {
        try {
            String json = (String) redisUtils.get(TaskConstants.HOTEL);
            if (json == null || json.isEmpty()) {
                return new Page<HotelVO>()
                        .setCurrent(page.getCurrent())
                        .setSize(page.getSize())
                        .setTotal(0)
                        .setRecords(Collections.emptyList());
            }

            HotelVO[] array = JsonUtils.fromJson(json, HotelVO[].class);
            List<HotelVO> list = array != null ? Arrays.asList(array) : Collections.emptyList();

            long total = list.size();

            return new Page<HotelVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(total)
                    .setRecords(new ArrayList<>(list));
        } catch (Exception ex) {
            log.error("getPageFallback error", ex);
            return new Page<HotelVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(0)
                    .setRecords(Collections.emptyList());
        }
    }
}
