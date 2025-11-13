package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.service.LocationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Location 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocationVO createLocation(Long userId, CreateLocationDTO createDTO) {
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
                .build();

        this.save(location);
        return toLocationVO(location);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocationVO updateLocation(Long userId, Long locationId, UpdateLocationDTO updateDTO) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
        }

        if (!location.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权修改他人的拍摄场地");
        }

        if (updateDTO.getName() != null) location.setName(updateDTO.getName());
        if (updateDTO.getType() != null) location.setType(updateDTO.getType());
        if (updateDTO.getStatus() != null) location.setStatus(updateDTO.getStatus());
        if (updateDTO.getLocationDescription() != null) location.setLocationDescription(updateDTO.getLocationDescription());
        if (updateDTO.getContactPhone() != null) location.setContactPhone(updateDTO.getContactPhone());
        if (updateDTO.getContactName() != null) location.setContactName(updateDTO.getContactName());
        if (updateDTO.getAddress() != null) location.setAddress(updateDTO.getAddress());
        if (updateDTO.getPrice() != null) location.setPrice(updateDTO.getPrice());

        this.updateById(location);
        return toLocationVO(location);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLocation(Long userId, Long locationId) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
        }

        if (!location.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权删除他人的拍摄场地");
        }

        location.setDeleted(true);
        this.updateById(location);
    }

    @Override
    public LocationVO getLocationById(Long locationId) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
        }
        return toLocationVO(location);
    }

    @Override
    public Page<LocationVO> getLocationPage(Page<Location> page, String keyword) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);
        wrapper.eq("status", 1); // 只显示可用的场地

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("type", keyword).or().like("address", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Location> locationPage = this.page(page, wrapper);
        Page<LocationVO> voPage = new Page<>(locationPage.getCurrent(), locationPage.getSize(), locationPage.getTotal());
        List<LocationVO> voList = locationPage.getRecords().stream()
                .map(this::toLocationVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocationVO createLocationByAdmin(CreateLocationDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
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
                .userId(currentUser.getId())
                .build();

        this.save(location);
        return toLocationVO(location);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
        }

        if (updateDTO.getName() != null) location.setName(updateDTO.getName());
        if (updateDTO.getType() != null) location.setType(updateDTO.getType());
        if (updateDTO.getStatus() != null) location.setStatus(updateDTO.getStatus());
        if (updateDTO.getLocationDescription() != null) location.setLocationDescription(updateDTO.getLocationDescription());
        if (updateDTO.getContactPhone() != null) location.setContactPhone(updateDTO.getContactPhone());
        if (updateDTO.getContactName() != null) location.setContactName(updateDTO.getContactName());
        if (updateDTO.getAddress() != null) location.setAddress(updateDTO.getAddress());
        if (updateDTO.getPrice() != null) location.setPrice(updateDTO.getPrice());

        this.updateById(location);
        return toLocationVO(location);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteLocationByAdmin(Long locationId) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
        }

        location.setDeleted(true);
        this.updateById(location);
    }

    @Override
    public Page<LocationVO> getLocationPageByAdmin(Page<Location> page, String keyword) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("type", keyword).or().like("address", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Location> locationPage = this.page(page, wrapper);
        Page<LocationVO> voPage = new Page<>(locationPage.getCurrent(), locationPage.getSize(), locationPage.getTotal());
        List<LocationVO> voList = locationPage.getRecords().stream()
                .map(this::toLocationVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public LocationVO getLocationByIdByAdmin(Long locationId) {
        Location location = this.getById(locationId);
        if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
        }
        return toLocationVO(location);
    }

    @Override
    public LocationVO toLocationVO(Location location) {
        if (location == null) {
            return null;
        }
        LocationVO vo = new LocationVO();
        BeanUtils.copyProperties(location, vo);
        return vo;
    }
}
