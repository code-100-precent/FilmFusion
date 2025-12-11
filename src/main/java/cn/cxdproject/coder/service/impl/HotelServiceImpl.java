package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
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
import java.time.LocalDateTime;
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
    @Loggable(
            type = LogType.HOTEL_CREATE,
            value = "Create hotel"
    )
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

        hotel.setCreatedAt(LocalDateTime.now());
        hotel.setUpdatedAt(LocalDateTime.now());

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
            cache.put(CaffeineConstants.HOTEL + hotelId, hotel);
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
    @Loggable(
            type = LogType.DRAMA_UPDATE,
            value = "Update hotel ID: #{#dramaId}"
    )
    public HotelVO updateHotelByAdmin(Long hotelId, UpdateHotelDTO updateDTO) {
        // 1. 校验是否存在且未删除
        Hotel existing = this.getById(hotelId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 2. 执行动态更新
        int updatedRows = hotelMapper.updateHotel(hotelId, updateDTO);

        // 3. 若无记录被更新
        if (updatedRows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 4. 重新加载最新数据（保证返回的是 DB 最新状态）
        Hotel updatedHotel = this.getById(hotelId);

        // 5. 更新缓存（注意：存的是对象，不是 ID！）
        cache.put(CaffeineConstants.HOTEL + hotelId, updatedHotel);

        // 6. 返回 VO
        return toHotelVO(updatedHotel);
    }


    @Override
    @Loggable(
            type = LogType.HOTEL_DELETE,
            value = "Delete hotel by ID: #{#hotelId}"
    )
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

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        Object store = cache.getIfPresent(CaffeineConstants.HOTEL + id);
        if (store != null) {
            return toHotelVO((Hotel) store);
        }
        return null;
    }

    @Override
    public List<HotelVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

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

        // 获取当前页的数据和总记录数
        List<Hotel> hotels = hotelMapper.getAdminPage(keyword, offset, size);
        Long total = hotelMapper.getTotal(keyword);

        List<HotelVO> voList = hotels.stream()
                .map(this::toHotelVO)
                .collect(Collectors.toList());

        return new Page<HotelVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }
}
