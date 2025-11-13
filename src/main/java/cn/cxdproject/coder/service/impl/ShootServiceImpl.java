package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.mapper.ShootMapper;
import cn.cxdproject.coder.service.ShootService;
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
 * Shoot 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class ShootServiceImpl extends ServiceImpl<ShootMapper, Shoot> implements ShootService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootVO createShoot(Long userId, CreateShootDTO createDTO) {
        Shoot shoot = Shoot.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .price(createDTO.getPrice())
                .status(createDTO.getStatus())
                .address(createDTO.getAddress())
                .phone(createDTO.getPhone())
                .contactName(createDTO.getContactName())
                .userId(userId)
                .build();

        this.save(shoot);
        return toShootVO(shoot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootVO updateShoot(Long userId, Long shootId, UpdateShootDTO updateDTO) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "协拍服务不存在");
        }

        if (!shoot.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权修改他人的协拍服务");
        }

        if (updateDTO.getName() != null) shoot.setName(updateDTO.getName());
        if (updateDTO.getDescription() != null) shoot.setDescription(updateDTO.getDescription());
        if (updateDTO.getPrice() != null) shoot.setPrice(updateDTO.getPrice());
        if (updateDTO.getStatus() != null) shoot.setStatus(updateDTO.getStatus());
        if (updateDTO.getAddress() != null) shoot.setAddress(updateDTO.getAddress());
        if (updateDTO.getPhone() != null) shoot.setPhone(updateDTO.getPhone());
        if (updateDTO.getContactName() != null) shoot.setContactName(updateDTO.getContactName());

        this.updateById(shoot);
        return toShootVO(shoot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShoot(Long userId, Long shootId) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "协拍服务不存在");
        }

        if (!shoot.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权删除他人的协拍服务");
        }

        shoot.setDeleted(true);
        this.updateById(shoot);
    }

    @Override
    public ShootVO getShootById(Long shootId) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "协拍服务不存在");
        }
        return toShootVO(shoot);
    }

    @Override
    public Page<ShootVO> getShootPage(Page<Shoot> page, String keyword) {
        QueryWrapper<Shoot> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);
        wrapper.eq("status", 1); // 只显示上线的服务

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword).or().like("address", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Shoot> shootPage = this.page(page, wrapper);
        Page<ShootVO> voPage = new Page<>(shootPage.getCurrent(), shootPage.getSize(), shootPage.getTotal());
        List<ShootVO> voList = shootPage.getRecords().stream()
                .map(this::toShootVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootVO createShootByAdmin(CreateShootDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
        }

        Shoot shoot = Shoot.builder()
                .name(createDTO.getName())
                .description(createDTO.getDescription())
                .price(createDTO.getPrice())
                .status(createDTO.getStatus())
                .address(createDTO.getAddress())
                .phone(createDTO.getPhone())
                .contactName(createDTO.getContactName())
                .userId(currentUser.getId())
                .build();

        this.save(shoot);
        return toShootVO(shoot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShootVO updateShootByAdmin(Long shootId, UpdateShootDTO updateDTO) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "协拍服务不存在");
        }

        if (updateDTO.getName() != null) shoot.setName(updateDTO.getName());
        if (updateDTO.getDescription() != null) shoot.setDescription(updateDTO.getDescription());
        if (updateDTO.getPrice() != null) shoot.setPrice(updateDTO.getPrice());
        if (updateDTO.getStatus() != null) shoot.setStatus(updateDTO.getStatus());
        if (updateDTO.getAddress() != null) shoot.setAddress(updateDTO.getAddress());
        if (updateDTO.getPhone() != null) shoot.setPhone(updateDTO.getPhone());
        if (updateDTO.getContactName() != null) shoot.setContactName(updateDTO.getContactName());

        this.updateById(shoot);
        return toShootVO(shoot);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShootByAdmin(Long shootId) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "协拍服务不存在");
        }

        shoot.setDeleted(true);
        this.updateById(shoot);
    }

    @Override
    public Page<ShootVO> getShootPageByAdmin(Page<Shoot> page, String keyword) {
        QueryWrapper<Shoot> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("description", keyword).or().like("address", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Shoot> shootPage = this.page(page, wrapper);
        Page<ShootVO> voPage = new Page<>(shootPage.getCurrent(), shootPage.getSize(), shootPage.getTotal());
        List<ShootVO> voList = shootPage.getRecords().stream()
                .map(this::toShootVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public ShootVO getShootByIdByAdmin(Long shootId) {
        Shoot shoot = this.getById(shootId);
        if (shoot == null || Boolean.TRUE.equals(shoot.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "协拍服务不存在");
        }
        return toShootVO(shoot);
    }

    @Override
    public ShootVO toShootVO(Shoot shoot) {
        if (shoot == null) {
            return null;
        }
        ShootVO vo = new ShootVO();
        BeanUtils.copyProperties(shoot, vo);
        return vo;
    }
}
