package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.service.DramaService;
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
 * Drama 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class DramaServiceImpl extends ServiceImpl<DramaMapper, Drama> implements DramaService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DramaVO createDrama(Long userId, CreateDramaDTO createDTO) {
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
                .build();

        this.save(drama);
        return toDramaVO(drama);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DramaVO updateDrama(Long userId, Long dramaId, UpdateDramaDTO updateDTO) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "电视剧备案不存在");
        }

        if (!drama.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权修改他人的电视剧备案");
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

        this.updateById(drama);
        return toDramaVO(drama);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDrama(Long userId, Long dramaId) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "电视剧备案不存在");
        }

        if (!drama.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权删除他人的电视剧备案");
        }

        drama.setDeleted(true);
        this.updateById(drama);
    }

    @Override
    public DramaVO getDramaById(Long dramaId) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "电视剧备案不存在");
        }
        return toDramaVO(drama);
    }

    @Override
    public Page<DramaVO> getDramaPage(Page<Drama> page, String keyword) {
        QueryWrapper<Drama> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("filing_num", keyword).or().like("prod_company", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Drama> dramaPage = this.page(page, wrapper);
        Page<DramaVO> voPage = new Page<>(dramaPage.getCurrent(), dramaPage.getSize(), dramaPage.getTotal());
        List<DramaVO> voList = dramaPage.getRecords().stream()
                .map(this::toDramaVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DramaVO createDramaByAdmin(CreateDramaDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
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
                .userId(currentUser.getId())
                .build();

        this.save(drama);
        return toDramaVO(drama);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DramaVO updateDramaByAdmin(Long dramaId, UpdateDramaDTO updateDTO) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "电视剧备案不存在");
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

        this.updateById(drama);
        return toDramaVO(drama);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDramaByAdmin(Long dramaId) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "电视剧备案不存在");
        }

        drama.setDeleted(true);
        this.updateById(drama);
    }

    @Override
    public Page<DramaVO> getDramaPageByAdmin(Page<Drama> page, String keyword) {
        QueryWrapper<Drama> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("filing_num", keyword).or().like("prod_company", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Drama> dramaPage = this.page(page, wrapper);
        Page<DramaVO> voPage = new Page<>(dramaPage.getCurrent(), dramaPage.getSize(), dramaPage.getTotal());
        List<DramaVO> voList = dramaPage.getRecords().stream()
                .map(this::toDramaVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public DramaVO getDramaByIdByAdmin(Long dramaId) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "电视剧备案不存在");
        }
        return toDramaVO(drama);
    }

    @Override
    public DramaVO toDramaVO(Drama drama) {
        if (drama == null) {
            return null;
        }
        DramaVO vo = new DramaVO();
        BeanUtils.copyProperties(drama, vo);
        return vo;
    }
}
