package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.mapper.LocationMapper;
import cn.cxdproject.coder.service.LocationService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
            createDTO.setCover("https://auto-avatar.oss-cn-beijing.aliyuncs.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251115152833_120_8.jpg");
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();

        this.save(location);
        return toLocationVO(location);
    }

    @Override
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
        if(updateDTO.getCover() != null) location.setCover(updateDTO.getCover());
        if(updateDTO.getImage() != null) location.setImage(updateDTO.getImage());

        location.setUpdatedAt(LocalDateTime.now());
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
                throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
            } else {
                throw new BusinessException(FORBIDDEN.code(), "无权删除他人的场地信息");
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
                throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
            }
            cache.asMap().put(CaffeineConstants.LOCATION + locationId, location);
            return toLocationVO(location);
        }
    }

    @Override
    public Page<LocationVO> getLocationPage(Page<Location> page, String keyword) {
        QueryWrapper<Location> wrapper = new QueryWrapper<>();

        wrapper.select("id", "name", "type", "status", "location_description", "cover","address","price")
                .eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("location_description", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Location> locationPage = this.page(page, wrapper);

        List<LocationVO> voList = locationPage.getRecords().stream()
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

        Page<LocationVO> voPage = new Page<>(locationPage.getCurrent(), locationPage.getSize(), locationPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

//    @Override
//    public LocationVO createLocationByAdmin(CreateLocationDTO createDTO) {
//        User currentUser = AuthContext.getCurrentUser();
//        if (currentUser == null) {
//            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
//        }
//        if (currentUser.getRole()== "user"){
//            throw new BusinessException(FORBIDDEN.code(), "无权创建文章");
//        }
//
//        Location location = Location.builder()
//                .name(createDTO.getName())
//                .type(createDTO.getType())
//                .status(createDTO.getStatus())
//                .locationDescription(createDTO.getLocationDescription())
//                .contactPhone(createDTO.getContactPhone())
//                .contactName(createDTO.getContactName())
//                .address(createDTO.getAddress())
//                .price(createDTO.getPrice())
//                .userId(currentUser.getId())
//                .build();
//
//        this.save(location);
//        return toLocationVO(location);
//    }

    @Override
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
        if(updateDTO.getCover() != null) location.setCover(updateDTO.getCover());
        if(updateDTO.getImage() != null) location.setImage(updateDTO.getImage());

        location.setUpdatedAt(LocalDateTime.now());
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
                throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
            }
        }
        cache.invalidate(CaffeineConstants.LOCATION+locationId);
    }

//    @Override
//    public Page<LocationVO> getLocationPageByAdmin(Page<Location> page, String keyword) {
//        QueryWrapper<Location> wrapper = new QueryWrapper<>();
//
//        wrapper.select("id", "name", "type", "status", "location_description", "cover","address","price");
//
//        if (keyword != null && !keyword.isEmpty()) {
//            wrapper.and(w -> w.like("location_description", keyword));
//        }
//
//        wrapper.orderByDesc("created_at");
//
//        Page<Location> locationPage = this.page(page, wrapper);
//
//        List<LocationVO> voList = locationPage.getRecords().stream()
//                .map(location -> new LocationVO(
//                        location.getId(),
//                        location.getName(),
//                        location.getStatus(),
//                        location.getType(),
//                        location.getLocationDescription(),
//                        location.getCover(),
//                        location.getAddress(),
//                        location.getPrice()
//                ))
//                .collect(Collectors.toList());
//
//        Page<LocationVO> voPage = new Page<>(locationPage.getCurrent(), locationPage.getSize(), locationPage.getTotal());
//        voPage.setRecords(voList);
//
//        return voPage;
//    }
//
//    @Override
//    public LocationVO getLocationByIdByAdmin(Long locationId) {
//        Object store = cache.asMap().get(CaffeineConstants.LOCATION + locationId);
//        if (store != null) {
//            return toLocationVO((Location) store);
//        } else {
//            Location location = this.getById(locationId);
//            if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
//                throw new NotFoundException(NOT_FOUND.code(), "拍摄场地不存在");
//            }
//            cache.asMap().put(CaffeineConstants.LOCATION + locationId, location);
//            return toLocationVO(location);
//        }
//    }

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
