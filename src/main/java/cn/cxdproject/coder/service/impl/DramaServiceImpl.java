package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.*;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.service.DramaService;
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
 * Drama 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class DramaServiceImpl extends ServiceImpl<DramaMapper, Drama> implements DramaService {

    private final DramaMapper dramaMapper;
    private final Cache<String, Object> cache;;
    private final RedisUtils redisUtils;

    public DramaServiceImpl(DramaMapper dramaMapper,
                            @Qualifier("cache") Cache<String, Object> cache,
                            RedisUtils redisUtils) {
        this.dramaMapper = dramaMapper;
        this.cache = cache;
        this.redisUtils = redisUtils;
    }


    @Override
    @Loggable(
            type = LogType.DRAMA_CREATE,
            value = "create drama"
    )
    public DramaVO createDramaByAdmin(Long userId, CreateDramaDTO createDTO) {
        if (createDTO.getImage() == null) {
            createDTO.setImage(Constants.DEFAULT_COVER);
            createDTO.setThumbImage(Constants.DEFAULT_THUMB_COVER);
        }

        Drama drama = Drama.builder()
                .name(createDTO.getName())
                .filingNum(createDTO.getFilingNum())
                .prodCompany(createDTO.getProdCompany())
                .crewDescription(createDTO.getCrewDescription())
                .dramaDescription(createDTO.getDramaDescription())
                .cast(createDTO.getCast())
                .shootLocation(createDTO.getShootLocation())
                .locationId(createDTO.getLocationId())
                .service(createDTO.getService())
                .serviceId(createDTO.getServiceId())
                .userId(userId)
                .image(createDTO.getImage())
                .thumbImage(createDTO.getThumbImage())
                .build();

        this.save(drama);
        return toDramaVO(drama);
    }

    @Override
    @CircuitBreaker(name = "dramaGetById", fallbackMethod = "getByIdFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    @Loggable(
            type = LogType.DRAMA_GET,
            value = "Get drama by ID: #{#dramaId}"
    )
    public DramaVO getDramaById(Long dramaId) {
        Object store = cache.asMap().get(CaffeineConstants.DRAMA + dramaId);
        if (store != null) {
            return toDramaVO((Drama) store);
        }else {
            Drama drama = this.getById(dramaId);
            if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.put(CaffeineConstants.DRAMA + dramaId, drama);
            return toDramaVO(drama);
        }
    }

    @Override
    @CircuitBreaker(name = "dramaGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
    @Loggable(
            type = LogType.DRAMA_USER_GET_PAGE,
            value = "User get drama page"
    )
    public List<DramaVO> getDramaPage(Long lastId, int size, String keyword) {
        List<Long> ids = dramaMapper.selectIds(lastId, size, keyword);
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        List<Drama> dramas = dramaMapper.selectBatchIds(ids);

        Map<Long, Drama> dramaMap = dramas.stream()
                .collect(Collectors.toMap(Drama::getId, a -> a));


        return ids.stream()
                .map(dramaMap::get)
                .filter(Objects::nonNull)
                .map(this::toDramaVO)
                .collect(Collectors.toList());
    }

    @Override
    @Loggable(
            type = LogType.DRAMA_UPDATE,
            value = "Update drama ID: #{#dramaId}"
    )
    public DramaVO updateDramaByAdmin(Long dramaId, UpdateDramaDTO updateDTO) {
        // 先校验文章是否存在且未被删除
        Drama existing = this.getById(dramaId);
        if (existing == null || Boolean.TRUE.equals(existing.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 执行动态更新（只更新非空字段）
        int updated = dramaMapper.updateDrama(dramaId, updateDTO);

        // 如果更新影响行数为 0，说明条件不满足（比如已被删除），也可视为未找到
        if (updated == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        // 重新加载最新数据（保证数据一致性）
        Drama updatedDrama = this.getById(dramaId);

        // 更新缓存
        cache.put(CaffeineConstants.DRAMA + dramaId, updatedDrama);

        // 返回 VO
        return toDramaVO(updatedDrama);
    }

    @Override
    @Loggable(
            type = LogType.DRAMA_DELETE,
            value = "delete drama ID: #{#dramaId}"
    )
    public void deleteDramaByAdmin(Long dramaId) {
        boolean updated = dramaMapper.update(null,
                Wrappers.<Drama>lambdaUpdate()
                        .set(Drama::getDeleted, true)
                        .eq(Drama::getId, dramaId)
                        .eq(Drama::getDeleted, false)
        ) > 0;
        if (!updated) {
            Drama drama = this.getById(dramaId);
            if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.DRAMA+dramaId);
    }

    @Override
    public DramaVO toDramaVO(Drama drama) {
        if (drama == null) {
            return null;
        }
        return DramaVO.builder()
                .id(drama.getId())
                .name(drama.getName())
                .filingNum(drama.getFilingNum())
                .prodCompany(drama.getProdCompany())
                .crewDescription(drama.getCrewDescription())
                .dramaDescription(drama.getDramaDescription())
                .cast(drama.getCast())
                .shootLocation(drama.getShootLocation())
                .locationId(drama.getLocationId())
                .service(drama.getService())
                .serviceId(drama.getServiceId())
                .userId(drama.getUserId())
                .createdAt(drama.getCreatedAt())
                .updatedAt(drama.getUpdatedAt())
                .image(drama.getImage())
                .thumbImage(drama.getThumbImage())
                .build();

    }

    @Override
    public DramaVO getByIdFallback(Long id,Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        Object store = cache.getIfPresent(CaffeineConstants.DRAMA + id);
        if (store != null) {
            return toDramaVO((Drama) store);
        }

        return null;
    }

    @Override
    public List<DramaVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

        if (e instanceof NotFoundException || e instanceof BusinessException) {
            throw (RuntimeException) e;
        }

        try {
            // 从 Redis 获取缓存的全量文章（假设是 ArticleVO[] 的 JSON）
            String json = (String) redisUtils.get(TaskConstants.DRAMA);
            if (json == null || json.isEmpty()) {
                return Collections.emptyList();
            }

            DramaVO[] array = JsonUtils.fromJson(json, DramaVO[].class);
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
    @Loggable(
            type = LogType.DRAMA_ADMIN_GET_PAGE,
            value = "Admin get drama page"
    )
    public Page<DramaVO> getDramaPageAdmin(Page<Drama> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Drama> dramas = dramaMapper.getAdminPage(keyword, offset, size);

        List<DramaVO> voList = dramas.stream()
                .map(this::toDramaVO)
                .collect(Collectors.toList());

        return new Page<DramaVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }
}
