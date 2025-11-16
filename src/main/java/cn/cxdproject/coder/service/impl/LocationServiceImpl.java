package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.constants.Constants;
import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.service.LocationService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Location 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {

    private final LocationMapper locationMapper;
    private final Cache<String, Object> cache;

    public LocationServiceImpl(
            LocationMapper locationMapper,
            @Qualifier("cache") Cache<String, Object> cache) {
        this.locationMapper = locationMapper;
        this.cache = cache;
    }

    @Override
    public LocationVO createLocation(Long userId, CreateLocationDTO createDTO) {
        if(createDTO.getCover()==null){
            createDTO.setCover(Constants.DEFAULT_COVER);
        }

        Location location = Location.builder()
                .name(createDTO.getName())
                .type(createDTO.getType())
                .status(createDTO.getStatus())
                .locationDescription(createDTO.getLocationDescription())
                .contactPhone(createDTO.getContactPhone())
                .contactName(createDTO.getContactName())
                .address(createDTO.getAddress())
                .price(createDTO.getPrice())
                .userId(userId)
                .image(createDTO.getImage())
                .build();

        this.save(location);
        return toLocationVO(location);
    }

    @Override
    public LocationVO updateLocation(Long userId, Long locationId, UpdateLocationDTO updateDTO) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (!location.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
        }

        if (updateDTO.getName() != null) location.setName(updateDTO.getName());
        if (updateDTO.getType() != null) location.setType(updateDTO.getType());
        if (updateDTO.getStatus() != null) location.setStatus(updateDTO.getStatus());
        if (updateDTO.getLocationDescription() != null) location.setLocationDescription(updateDTO.getLocationDescription());
        if (updateDTO.getContactPhone() != null) location.setContactPhone(updateDTO.getContactPhone());
        if (updateDTO.getContactName() != null) location.setContactName(updateDTO.getContactName());
        if (updateDTO.getAddress() != null) location.setAddress(updateDTO.getAddress());
        if (updateDTO.getPrice() != null) location.setPrice(updateDTO.getPrice());
        if(updateDTO.getCover() != null) location.setCover(updateDTO.getCover());
        if(updateDTO.getImage() != null) location.setImage(updateDTO.getImage());

        cache.asMap().put(CaffeineConstants.LOCATION + locationId, location);
        this.updateById(location);
        return toLocationVO(location);
    }

    @Override
    public void deleteLocation(Long userId, Long locationId) {
        boolean updated = locationMapper.update(null,
                Wrappers.<Location>lambdaUpdate()
                        .set(Location::getDeleted, true)
                        .eq(Location::getId, locationId)
                        .eq(Location::getUserId, userId)
                        .eq(Location::getDeleted, false)
        ) > 0;

        if (!updated) {
            Location location = this.getById(locationId);
            if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            } else {
                throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
            }
        }
        cache.invalidate(CaffeineConstants.LOCATION+locationId);
    }


    @Override
    public LocationVO getLocationById(Long locationId) {
        Object store = cache.asMap().get(CaffeineConstants.LOCATION + locationId);
        if (store != null) {
            return toLocationVO((Location) store);
        } else {
            Location location = this.getById(locationId);
            if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.LOCATION + locationId, location);
            return toLocationVO(location);
        }
    }

    @Override
    public Page<LocationVO> getLocationPage(Page<Location> page, String keyword) {

        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Location> locations = locationMapper.getPage(keyword, offset, size);

        List<LocationVO> voList = locations.stream()
                .map(location -> new LocationVO(
                        location.getId(),
                        location.getName(),
                        location.getStatus(),
                        location.getType(),
                        location.getLocationDescription(),
                        location.getCover(),
                        location.getAddress(),
                        location.getPrice()
                ))
                .collect(Collectors.toList());

        return new Page<LocationVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);

    }


    @Override
    public LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getName() != null) location.setName(updateDTO.getName());
        if (updateDTO.getType() != null) location.setType(updateDTO.getType());
        if (updateDTO.getStatus() != null) location.setStatus(updateDTO.getStatus());
        if (updateDTO.getLocationDescription() != null) location.setLocationDescription(updateDTO.getLocationDescription());
        if (updateDTO.getContactPhone() != null) location.setContactPhone(updateDTO.getContactPhone());
        if (updateDTO.getContactName() != null) location.setContactName(updateDTO.getContactName());
        if (updateDTO.getAddress() != null) location.setAddress(updateDTO.getAddress());
        if (updateDTO.getPrice() != null) location.setPrice(updateDTO.getPrice());
        if(updateDTO.getCover() != null) location.setCover(updateDTO.getCover());
        if(updateDTO.getImage() != null) location.setImage(updateDTO.getImage());

        cache.asMap().put(CaffeineConstants.LOCATION + locationId, location);
        this.updateById(location);
        return toLocationVO(location);
    }

    @Override
    public void deleteLocationByAdmin(Long locationId) {
        boolean updated = locationMapper.update(null,
                Wrappers.<Location>lambdaUpdate()
                        .set(Location::getDeleted, true)
                        .eq(Location::getId, locationId)
                        .eq(Location::getDeleted, false)
        ) > 0;

        if (!updated) {
            Location location = this.getById(locationId);
            if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.LOCATION+locationId);
    }


    @Override
    public LocationVO toLocationVO(Location location) {
        if (location == null) {
            return null;
        }
       return LocationVO.builder()
               .id(location.getId())
               .name(location.getName())
               .type(location.getType())
               .status(location.getStatus())
               .locationDescription(location.getLocationDescription())
               .contactPhone(location.getContactPhone())
               .contactName(location.getContactName())
               .address(location.getAddress())
               .price(location.getPrice())
               .userId(location.getUserId())
               .cover(location.getCover())
               .createdAt(location.getCreatedAt())
               .updatedAt(location.getUpdatedAt())
               .image(location.getImage())
               .build();
    }
}
