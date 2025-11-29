package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.DramaVO;
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
    public List<ShootVO> getShootPage(Long lastId, int size, String keyword) {
        List<Long> ids = shootMapper.selectIds(lastId, size, keyword);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Shoot> shoots = shootMapper.selectBatchIds(ids);

        Map<Long, Shoot> shootMap = shoots.stream()
                .collect(Collectors.toMap(Shoot::getId, a -> a));

        return ids.stream()
                .map(shootMap::get)
                .filter(Objects::nonNull)
                .map(this::toShootVO)
                .collect(Collectors.toList());
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
    public List<ShootVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        try {
            // 从 Redis 获取缓存的全量文章（假设是 ArticleVO[] 的 JSON）
            String json = (String) redisUtils.get(TaskConstants.SHOOT);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            ShootVO[] array = JsonUtils.fromJson(json, ShootVO[].class);
            if (array == null || array.length == 0) {
                return Collections.emptyList();
            }

            // 直接取前 N 条（N = min(size, 缓存长度)）
            // 注意：这里忽略 lastId 和 keyword，因为 fallback 只提供静态兜底数据
            int take = Math.min(size, array.length);
            return new ArrayList<>(Arrays.asList(array).subList(0, take));

        } catch (Exception ex) {
            log.error("Fallback failed", ex);
            return Collections.emptyList();
        }
    }

    @Override
    public Page<ShootVO> getShootPageAdmin(Page<Shoot> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Shoot> shoots = shootMapper.getAdminPage(keyword, offset, size);

        List<ShootVO> voList = shoots.stream()
                .map(this::toShootVO)
                .collect(Collectors.toList());

        return new Page<ShootVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }
}
