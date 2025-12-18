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
import cn.cxdproject.coder.model.entity.*;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.HotelVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.service.DramaService;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Slf4j
@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements HotelService {

    private final HotelMapper hotelMapper;
    private final Cache<String, Object> cache;
    public  final RedisUtils redisUtils;
    private final ObjectProvider<HotelService> hotelServiceProvider;


    public HotelServiceImpl(
            HotelMapper hotelMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils, ObjectProvider<HotelService> hotelServiceProvider) {
        this.hotelMapper = hotelMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
        this.hotelServiceProvider = hotelServiceProvider;
    }


    //管理端创建
    @Override
    @Loggable(
            type = LogType.HOTEL_CREATE,
            value = "Create hotel"
    )
    public HotelVO createHotelByAdmin(Long userId, CreateHotelDTO createDTO) {

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

    //根据id查询
    @Override
    @CircuitBreaker(name = "hotelGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public HotelVO getHotelById(Long hotelId){
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


    //客户端批量查询（游标分页）
    @Override
    @CircuitBreaker(name = "hotelGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public List<HotelVO> getHotelPage(Long lastId, int size, String keyword)  {
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

    //更新数据（仅限管理端）
    @Override
    @Loggable(
            type = LogType.DRAMA_UPDATE,
            value = "Update hotel ID: #{#hotelId}"
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


    //删除信息（仅限管理端）
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

    //id查询降级接口
    @Override
    public HotelVO getByIdFallback(Long id,Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        HotelVO hotel = redisUtils.get(TaskConstants.HOTEL + id, HotelVO.class);
        if (hotel != null) {
            return hotel;
        }
        return null;
    }

    //客户端游标分页降级接口
    @Override
    public List<HotelVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        try {
            // 从 Redis 获取缓存的全量文章（假设是 ArticleVO[] 的 JSON）
            String json = (String) redisUtils.get(TaskConstants.HOTEL_PAGE);
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

    //管理端分页查询
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

    @Override
    public HotelVO getHotelByIdWithTimeout(Long hotelId) {
        try {
            CompletableFuture<HotelVO> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return hotelServiceProvider.getObject().
                                    getHotelById(hotelId);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return future.get(Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getByIdFallback(hotelId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<HotelVO> getHotelPageWithTimeout(Long lastId, int size, String keyword) {
        try {
            CompletableFuture<List<HotelVO>> future =
                    CompletableFuture.supplyAsync(() -> {
                        try {
                            return hotelServiceProvider.getObject()
                                    .getHotelPage(lastId, size, keyword);
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
