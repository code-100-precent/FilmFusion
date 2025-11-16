package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.constants.Constants;
import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.service.DramaService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Drama 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class DramaServiceImpl extends ServiceImpl<DramaMapper, Drama> implements DramaService {

    private final DramaMapper dramaMapper;
    private final Cache<String, Object> cache;

    public DramaServiceImpl(DramaMapper dramaMapper,  @Qualifier("cache") Cache<String, Object> cache) {
        this.dramaMapper = dramaMapper;
        this.cache = cache;
    }


    @Override
    public DramaVO createDrama(Long userId, CreateDramaDTO createDTO) {
        if(createDTO.getCover()==null){
            createDTO.setCover(Constants.DEFAULT_COVER);
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
                .cover(createDTO.getCover())
                .build();

        this.save(drama);
        return toDramaVO(drama);
    }

    @Override
    public DramaVO updateDrama(Long userId, Long dramaId, UpdateDramaDTO updateDTO) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (!drama.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
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
        if(updateDTO.getCover() != null) drama.setCover(updateDTO.getCover());
        if(updateDTO.getImage() != null) drama.setImage(updateDTO.getImage());

        cache.asMap().put(CaffeineConstants.DRAMA + dramaId, drama);
        this.updateById(drama);
        return toDramaVO(drama);
    }

    @Override
    public void deleteDrama(Long userId, Long dramaId) {
        boolean updated = dramaMapper.update(null,
                Wrappers.<Drama>lambdaUpdate()
                        .set(Drama::getDeleted, true)
                        .eq(Drama::getId, dramaId)
                        .eq(Drama::getUserId, userId)
                        .eq(Drama::getDeleted, false)
        ) > 0;
        if (!updated) {
            Drama drama = this.getById(dramaId);
            if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            } else {
                throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
            }
        }
        cache.invalidate(CaffeineConstants.DRAMA+dramaId);
    }

    @Override
    public DramaVO getDramaById(Long dramaId) {
        Object store = cache.asMap().get(CaffeineConstants.DRAMA + dramaId);
        if (store != null) {
            return toDramaVO((Drama) store);
        }else {
            Drama drama = this.getById(dramaId);
            if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.DRAMA + dramaId, dramaId);
            return toDramaVO(drama);
        }
    }

    @Override
    public Page<DramaVO> getDramaPage(Page<Drama> page, String keyword) {

        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Drama> articles = dramaMapper.getPage(keyword, offset, size);

        List<DramaVO> voList = articles.stream()
                .map(drama -> new DramaVO(
                        drama.getId(),
                        drama.getName(),
                        drama.getDramaDescription(),
                        drama.getCast(),
                        drama.getCover()
                ))
                .collect(Collectors.toList());

        return new Page<DramaVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);

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
        if(updateDTO.getCover() != null) drama.setCover(updateDTO.getCover());
        if(updateDTO.getImage() != null) drama.setImage(updateDTO.getImage());

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
                .cover(drama.getCover())
                .createdAt(drama.getCreatedAt())
                .updatedAt(drama.getUpdatedAt())
                .image(drama.getImage())
                .build();

    }
}
