package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.mapper.HotelMapper;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.CreateHotelDTO;
import cn.cxdproject.coder.model.dto.UpdateHotelDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Hotel;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.ArticleVO;
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
        if (createDTO.getImage() == null) {
            createDTO.setImage(Constants.DEFAULT_COVER);
            createDTO.setThumbImage(Constants.DEFAULT_THUMB_COVER);
        }
        Hotel hotel = Hotel.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .address(createDTO.getAddress())
                .managerName(createDTO.getManagerName())
                .managerPhone(createDTO.getManagerPhone())
                .image(createDTO.getImage())
                .userId(userId)
                .thumbImage(createDTO.getThumbImage())
                .latitude(createDTO.getLatitude())
                .longitude(createDTO.getLongitude())
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
    public List<HotelVO> getHotelPage(Long lastId, int size, String keyword) {
        List<Long> ids = hotelMapper.selectIds(lastId, size, keyword);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hotel> hotels = hotelMapper.selectBatchIds(ids);

        Map<Long, Hotel> hotelMap = hotels.stream()
                .collect(Collectors.toMap(Hotel::getId, a -> a));

        return ids.stream()
                .map(hotelMap::get)
                .filter(Objects::nonNull)
                .map(this::toHotelVO)
                .collect(Collectors.toList());
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
        if (updateDTO.getImage() != null) hotel.setImage(updateDTO.getImage());
        if (updateDTO.getThumbImage() != null) hotel.setThumbImage(updateDTO.getThumbImage());
        if (updateDTO.getLongitude() != null) hotel.setLongitude(updateDTO.getLongitude());
        if (updateDTO.getLatitude() != null) hotel.setLatitude(updateDTO.getLatitude());

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
                .updatedAt(hotel.getUpdatedAt())
                .createdAt(hotel.getCreatedAt())
                .userId(hotel.getUserId())
                .thumbImage(hotel.getThumbImage())
                .longitude(hotel.getLongitude())
                .latitude(hotel.getLatitude())
                .build();
    }

    @Override
    public HotelVO getByIdFallback(Long id,Throwable e) {
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
    public List<HotelVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        try {
            // 从 Redis 获取缓存的全量文章（假设是 ArticleVO[] 的 JSON）
            String json = (String) redisUtils.get(TaskConstants.HOTEL);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            HotelVO[] array = JsonUtils.fromJson(json, HotelVO[].class);
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
    public Page<HotelVO> getHotelPageAdmin(Page<Hotel> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Hotel> hotels = hotelMapper.getAdminPage(keyword, offset, size);

        List<HotelVO> voList = hotels.stream()
                .map(this::toHotelVO)
                .collect(Collectors.toList());

        return new Page<HotelVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }
}
