package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
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
import cn.cxdproject.coder.service.PolicyService;
import cn.cxdproject.coder.service.ShootService;
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
import org.springframework.context.annotation.Lazy;
import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
    private final ObjectProvider<ShootService> shootServiceProvider;

    public ShootServiceImpl(
            ShootMapper shootMapper,
            @Qualifier("cache") Cache<String, Object> cache,
            RedisUtils redisUtils, ObjectProvider<ShootService> shootServiceProvider) {
        this.shootMapper = shootMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
        this.shootServiceProvider = shootServiceProvider;
    }

    //创建shoot数据
    @Override
    @Loggable(
            type = LogType.SHOOT_CREATE,
            value = "Create shoot"
    )
    public ShootVO createShootByAdmin(Long userId, CreateShootDTO createDTO) {

        Shoot shoot = Shoot.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .price(createDTO.getPrice())
                .status(createDTO.getStatus())
                .address(createDTO.getAddress())
                .phone(createDTO.getPhone())
                .contactName(createDTO.getContactName())
                .userId(userId)
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .build();

        shoot.setCreatedAt(LocalDateTime.now());
        shoot.setUpdatedAt(LocalDateTime.now());

        this.save(shoot);
        return toShootVO(shoot);
    }

    //根据id获取单个数据
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
            cache.put(CaffeineConstants.SHOOT + shootId, shoot);
            return toShootVO(shoot);
        }
    }

    //客户端批量查询数据（游标分页）
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

    //管理端更新数据
    @Override
    @Bulkhead(name = "update", type = Bulkhead.Type.SEMAPHORE)
    @Loggable(
            type = LogType.SHOOT_UPDATE,
            value = "Update shoot ID: #{#shootId}"
    )
    public ShootVO updateShootByAdmin(Long shootId, UpdateShootDTO updateDTO) {
        // 1. 校验是否存在且未被删除
        Shoot existing = this.getById(shootId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 3. 执行动态更新
        int updatedRows = shootMapper.updateShoot(shootId, updateDTO);

        // 4. 若无记录被更新（可能已被删除）
        if (updatedRows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 5. 重新加载最新数据（保证缓存和返回的是最新状态）
        Shoot updatedShoot = this.getById(shootId);

        // 6. 更新缓存
        cache.put(CaffeineConstants.SHOOT + shootId, updatedShoot);

        // 7. 返回 VO
        return toShootVO(updatedShoot);
    }

    //管理员删除数据
    @Override
    @Loggable(
            type = LogType.SHOOT_DELETE,
            value = "Delete shoot by ID: #{#shootId}"
    )
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
                .createdAt(shoot.getCreatedAt())
                .updatedAt(shoot.getUpdatedAt())
                .image(shoot.getImage())
                .thumbImage(shoot.getThumbImage())
                .build();
    }

    //单个数据查询降级接口
    @Override
    public ShootVO getByIdFallback(Long id,Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        Object store = cache.getIfPresent(CaffeineConstants.SHOOT + id);
        if (store != null) {
            return toShootVO((Shoot) store);
        }
        return null;
    }

    //客户端批量查询降级接口
    @Override
    public List<ShootVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

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

            int take = Math.min(size, array.length);
            return new ArrayList<>(Arrays.asList(array).subList(0, take));

        } catch (Exception ex) {
            log.error("Fallback failed", ex);
            return Collections.emptyList();
        }
    }

    //管理端批量查询
    @Override
    public Page<ShootVO> getShootPageAdmin(Page<Shoot> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        // 获取当前页的数据和总记录数
        List<Shoot> shoots = shootMapper.getAdminPage(keyword, offset, size);
        Long total = shootMapper.getTotal(keyword);

        List<ShootVO> voList = shoots.stream()
                .map(this::toShootVO)
                .collect(Collectors.toList());

        return new Page<ShootVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }

    @Override
    public ShootVO getShootByIdWithTimeout(Long shootId) {
        try {
            CompletableFuture<ShootVO> future =
                    CompletableFuture.supplyAsync(() -> shootServiceProvider.getObject()
                            .getShootById(shootId));
            return future.get(Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getByIdFallback(shootId, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ShootVO> getShootPageWithTimeout(Long lastId, int size, String keyword) {
        try {
            CompletableFuture<List<ShootVO>> future =
                    CompletableFuture.supplyAsync(() -> shootServiceProvider.getObject()
                            .getShootPage(lastId, size, keyword));
            return future.get(Constants.TIME, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return getPageFallback(lastId, size, keyword, e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
