package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.*;
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
    @Bulkhead(name = "add", type = Bulkhead.Type.SEMAPHORE)
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
    public DramaVO getDramaById(Long dramaId) {
        Object store = cache.asMap().get(CaffeineConstants.DRAMA + dramaId);
        if (store != null) {
            return toDramaVO((Drama) store);
        }else {
            Drama drama = this.getById(dramaId);
            if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.DRAMA + dramaId, drama);
            return toDramaVO(drama);
        }
    }

    @Override
    @CircuitBreaker(name = "dramaGetPage", fallbackMethod = "getPageFallback")
    @Bulkhead(name = "get", type = Bulkhead.Type.SEMAPHORE)
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
    public DramaVO updateDramaByAdmin(Long dramaId, UpdateDramaDTO updateDTO) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getName() != null) drama.setName(updateDTO.getName());
        if (updateDTO.getFilingNum() != null) drama.setFilingNum(updateDTO.getFilingNum());
        if (updateDTO.getProdCompany() != null) drama.setProdCompany(updateDTO.getProdCompany());
        if (updateDTO.getCrewDescription() != null) drama.setCrewDescription(updateDTO.getCrewDescription());
        if (updateDTO.getDramaDescription() != null) drama.setDramaDescription(updateDTO.getDramaDescription());
        if (updateDTO.getCast() != null) drama.setCast(updateDTO.getCast());
        if (updateDTO.getShootLocation() != null) drama.setShootLocation(updateDTO.getShootLocation());
        if (updateDTO.getLocationId() != null) drama.setLocationId(updateDTO.getLocationId());
        if (updateDTO.getService() != null) drama.setService(updateDTO.getService());
        if (updateDTO.getServiceId() != null) drama.setServiceId(updateDTO.getServiceId());
        if(updateDTO.getImage() != null) drama.setImage(updateDTO.getImage());
        if(updateDTO.getThumbImage() != null) drama.setThumbImage(updateDTO.getThumbImage());

        drama.setUpdatedAt(LocalDateTime.now());
        cache.asMap().put(CaffeineConstants.DRAMA + dramaId, dramaId);
        this.updateById(drama);
        return toDramaVO(drama);
    }

    @Override
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
        Object store;
        store = cache.getIfPresent(CaffeineConstants.DRAMA + id);
        if (store != null) {
            return toDramaVO((Drama) store);
        }
        store = redisUtils.get(RedisKeyConstants.DRAMA+id);
        if (store != null) {
            Drama drama = JsonUtils.fromJson((String) store, Drama.class);
            return toDramaVO(drama);
        }
        return null;
    }

    @Override
    public List<DramaVO> getPageFallback(Long lastId, int size, String keyword, Throwable e) {

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
