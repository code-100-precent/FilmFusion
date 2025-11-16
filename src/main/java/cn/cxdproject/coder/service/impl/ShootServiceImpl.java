package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.constants.Constants;
import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.mapper.ShootMapper;
import cn.cxdproject.coder.service.ShootService;
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
 * Shoot 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class ShootServiceImpl extends ServiceImpl<ShootMapper, Shoot> implements ShootService {

    private final ShootMapper shootMapper;
    private final Cache<String, Object> cache;

    public ShootServiceImpl(
            ShootMapper shootMapper,
            @Qualifier("cache") Cache<String, Object> cache) {
        this.shootMapper = shootMapper;
        this.cache = cache;
    }

    @Override
    public ShootVO createShoot(Long userId, CreateShootDTO createDTO) {
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
                .image(createDTO.getImage())
                .build();

        this.save(shoot);
        return toShootVO(shoot);
    }

    @Override
    public ShootVO updateShoot(Long userId, Long shootId, UpdateShootDTO updateDTO) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (!shoot.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
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

        cache.asMap().put(CaffeineConstants.SHOOT + shootId, shoot);
        this.updateById(shoot);
        return toShootVO(shoot);
    }

    @Override
    public void deleteShoot(Long userId, Long shootId) {
        boolean updated = shootMapper.update(null,
                Wrappers.<Shoot>lambdaUpdate()
                        .set(Shoot::getDeleted, true)
                        .eq(Shoot::getId, shootId)
                        .eq(Shoot::getUserId, userId)
                        .eq(Shoot::getDeleted, false)
        ) > 0;

        if (!updated) {
            Shoot shoot = this.getById(shootId);
            if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            } else {
                throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
            }
        }
        cache.invalidate(CaffeineConstants.SHOOT+shootId);
    }

    @Override
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
    public Page<ShootVO> getShootPage(Page<Shoot> page, String keyword) {
        long current = page.getCurrent(); // MyBatis-Plus 页码从 1 开始
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Shoot> shoots = shootMapper.getPage(keyword, offset, size);

        List<ShootVO> voList = shoots.stream()
                .map(shoot -> new ShootVO(
                        shoot.getId(),
                        shoot.getName(),
                        shoot.getDescription(),
                        shoot.getPrice(),
                        shoot.getStatus(),
                        shoot.getCover(),
                        shoot.getAddress()
                ))
                .collect(Collectors.toList());

        return new Page<ShootVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);

    }

    @Override
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

        shoot.setUpdatedAt(LocalDateTime.now());
        cache.asMap().put(CaffeineConstants.SHOOT + shootId, shoot);
        this.updateById(shoot);
        return toShootVO(shoot);
    }

    @Override
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
                .build();
    }
}
