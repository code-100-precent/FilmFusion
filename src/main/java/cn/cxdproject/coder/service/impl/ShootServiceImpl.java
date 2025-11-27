package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.mapper.ShootMapper;
import cn.cxdproject.coder.service.ShootService;
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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Shoot 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class ShootServiceImpl extends ServiceImpl<ShootMapper, Shoot> implements ShootService {

    private final ShootMapper shootMapper;
    private final Cache<String, Object> cache;
    private final RedisUtils redisUtils;

    public ShootServiceImpl(
            ShootMapper shootMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils) {
        this.shootMapper = shootMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
    }

    @Override
    @Bulkhead(name = "add", type = Bulkhead.Type.SEMAPHORE)
    public ShootVO createShootByAdmin(Long userId, CreateShootDTO createDTO) {
        if(createDTO.getCover()==null){
            createDTO.setCover(Constants.DEFAULT_COVER);
        }

        Shoot shoot = Shoot.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .price(createDTO.getPrice())
                .status(createDTO.getStatus())
                .address(createDTO.getAddress())
                .phone(createDTO.getPhone())
                .contactName(createDTO.getContactName())
                .userId(userId)
                .cover(createDTO.getCover())
                .image(createDTO.getImage())
                .thumbCover(createDTO.getThumbCover())
                .thumbImage(createDTO.getThumbImage())
                .build();

        this.save(shoot);
        return toShootVO(shoot);
    }

    @Override
    @CircuitBreaker(name = "shootGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public ShootVO getShootById(Long shootId) {
        Object store = cache.asMap().get(CaffeineConstants.SHOOT + shootId);
        if (store != null) {
            return toShootVO((Shoot) store);
        } else {
            Shoot shoot = this.getById(shootId);
            if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.SHOOT + shootId, shoot);
            return toShootVO(shoot);
        }
    }

    @Override
    @CircuitBreaker(name = "shootGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    public Page<ShootVO> getShootPage(Page<Shoot> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Long> ids = shootMapper.getPageShootIds(keyword, offset, size);
        long total = shootMapper.countByKeyword(keyword);

        if (ids.isEmpty()) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 1. 批量从 Redis 获取缓存
        List<String> keys = ids.stream().map(id -> RedisKeyConstants.SHOOT + id).collect(Collectors.toList());
        List<String> cachedJsons = redisUtils.multiGet(keys);

        // 2. 构建Shoot列表：优先用缓存，缺失的记录 ID
        List<Shoot> shoots = new ArrayList<>(Collections.nCopies(ids.size(), null));
        List<Long> missingIds = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            String json = cachedJsons.get(i);
            if (json != null) {
                try {
                    shoots.set(i, JsonUtils.fromJson(json, Shoot.class));
                } catch (Exception e) {
                    missingIds.add(ids.get(i));
                }
            } else {
                missingIds.add(ids.get(i));
            }
        }

        if (!missingIds.isEmpty()) {
            List<Shoot> dbShoots = shootMapper.selectBatchIds(missingIds);
            Map<Long, Shoot> dbMap = dbShoots.stream()
                    .peek(shoot -> {
                        // 回填 Redis 缓存
                        redisUtils.set(
                                RedisKeyConstants.SHOOT + shoot.getId(),
                                JsonUtils.toJson(shoot),
                                Duration.ofMinutes(30)
                        );
                        cache.put(CaffeineConstants.SHOOT+shoot.getId(),shoot);
                    })
                    .collect(Collectors.toMap(Shoot::getId, a -> a));

            // 填回原位置
            for (int i = 0; i < ids.size(); i++) {
                if (shoots.get(i) == null) {
                    shoots.set(i, dbMap.get(ids.get(i)));
                }
            }
        }

        List<ShootVO> voList = shoots.stream()
                .map(this::toShootVO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new Page<ShootVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);

    }

    @Override
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    public ShootVO updateShootByAdmin(Long shootId, UpdateShootDTO updateDTO) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getName() != null) shoot.setName(updateDTO.getName());
        if (updateDTO.getDescription() != null) shoot.setDescription(updateDTO.getDescription());
        if (updateDTO.getPrice() != null) shoot.setPrice(updateDTO.getPrice());
        if (updateDTO.getStatus() != null) shoot.setStatus(updateDTO.getStatus());
        if (updateDTO.getAddress() != null) shoot.setAddress(updateDTO.getAddress());
        if (updateDTO.getPhone() != null) shoot.setPhone(updateDTO.getPhone());
        if (updateDTO.getContactName() != null) shoot.setContactName(updateDTO.getContactName());
        if (updateDTO.getCover() != null) shoot.setCover(updateDTO.getCover());
        if (updateDTO.getImage() != null) shoot.setImage(updateDTO.getImage());
        if (updateDTO.getThumbCover() != null) shoot.setThumbCover(updateDTO.getThumbCover());
        if (updateDTO.getThumbImage() != null) shoot.setThumbImage(updateDTO.getThumbImage());

        shoot.setUpdatedAt(LocalDateTime.now());
        cache.asMap().put(CaffeineConstants.SHOOT + shootId, shoot);
        this.updateById(shoot);
        return toShootVO(shoot);
    }

    @Override
    @Bulkhead(name = "delete", type = Bulkhead.Type.SEMAPHORE)
    public void deleteShootByAdmin(Long shootId) {
        boolean updated = shootMapper.update(null,
                Wrappers.<Shoot>lambdaUpdate()
                        .set(Shoot::getDeleted, true)
                        .eq(Shoot::getId, shootId)
                        .eq(Shoot::getDeleted, false)
        ) > 0;

        if (!updated) {
            Shoot shoot = this.getById(shootId);
            if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.SHOOT+shootId);
    }

    @Override
    public ShootVO toShootVO(Shoot shoot) {
        if (shoot == null) {
            return null;
        }
        return ShootVO.builder()
                .id(shoot.getId())
                .name(shoot.getName())
                .description(shoot.getDescription())
                .price(shoot.getPrice())
                .status(shoot.getStatus())
                .address(shoot.getAddress())
                .phone(shoot.getPhone())
                .contactName(shoot.getContactName())
                .userId(shoot.getUserId())
                .cover(shoot.getCover())
                .createdAt(shoot.getCreatedAt())
                .updatedAt(shoot.getUpdatedAt())
                .image(shoot.getImage())
                .thumbCover(shoot.getThumbCover())
                .thumbImage(shoot.getThumbImage())
                .build();
    }

    @Override
    public ShootVO getByIdFallback(Long id,Throwable e) {
        Object store;
        store = cache.getIfPresent(CaffeineConstants.SHOOT + id);
        if (store != null) {
            return toShootVO((Shoot) store);
        }
        store = redisUtils.get(RedisKeyConstants.SHOOT+id);
        if (store != null) {
            Shoot shoot = JsonUtils.fromJson((String) store, Shoot.class);
            return toShootVO(shoot);
        }
        return null;
    }

    @Override
    public Page<ShootVO> getPageFallback(Page<Shoot> page, String keyword, Throwable e) {
        try {
            String json = (String) redisUtils.get(TaskConstants.SHOOT);
            if (json == null || json.isEmpty()) {
                return new Page<ShootVO>()
                        .setCurrent(page.getCurrent())
                        .setSize(page.getSize())
                        .setTotal(0)
                        .setRecords(Collections.emptyList());
            }

            ShootVO[] array = JsonUtils.fromJson(json, ShootVO[].class);
            List<ShootVO> list = array != null ? Arrays.asList(array) : Collections.emptyList();

            long total = list.size();

            return new Page<ShootVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(total)
                    .setRecords(new ArrayList<>(list));
        } catch (Exception ex) {
            log.error("getPageFallback error", ex);
            return new Page<ShootVO>()
                    .setCurrent(page.getCurrent())
                    .setSize(page.getSize())
                    .setTotal(0)
                    .setRecords(Collections.emptyList());
        }
    }
}
