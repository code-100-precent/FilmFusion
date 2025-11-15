package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.BannerConstants;
import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.constants.DramaConstants;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.mapper.DramaMapper;
import cn.cxdproject.coder.service.DramaService;
import cn.cxdproject.coder.utils.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            createDTO.setCover("https://auto-avatar.oss-cn-beijing.aliyuncs.com/%E5%BE%AE%E4%BF%A1%E5%9B%BE%E7%89%87_20251115152833_120_8.jpg");
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
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
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
            throw new NotFoundException(NOT_FOUND.code(), DramaConstants.NOT_FIND);
        }

        if (!drama.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), DramaConstants.NO_PERMISSION);
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
                throw new NotFoundException(NOT_FOUND.code(), DramaConstants.NOT_FIND);
            } else {
                throw new BusinessException(FORBIDDEN.code(), DramaConstants.NO_PERMISSION);
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
                throw new NotFoundException(NOT_FOUND.code(), DramaConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.DRAMA + dramaId, dramaId);
            return toDramaVO(drama);
        }
    }

    @Override
    public Page<DramaVO> getDramaPage(Page<Drama> page, String keyword) {
        QueryWrapper<Drama> wrapper = new QueryWrapper<>();

        wrapper.select("id", "name", "drama_description", "cast", "cover")
                .eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Drama> dramaPage = this.page(page, wrapper);

        List<DramaVO> voList = dramaPage.getRecords().stream()
                .map(drama -> new DramaVO(
                        drama.getId(),
                        drama.getName(),
                        drama.getDramaDescription(),
                        drama.getCast(),
                        drama.getCover()
                ))
                .collect(Collectors.toList());

        Page<DramaVO> voPage = new Page<>(dramaPage.getCurrent(), dramaPage.getSize(), dramaPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

//    @Override
//    public DramaVO createDramaByAdmin(CreateDramaDTO createDTO) {
//        User currentUser = AuthContext.getCurrentUser();
//        if (currentUser == null) {
//            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
//        }
//
//        if (currentUser.getRole()== "user"){
//            throw new BusinessException(FORBIDDEN.code(), "无权创建文章");
//        }
//
//        Drama drama = Drama.builder()
//                .name(createDTO.getName())
//                .filingNum(createDTO.getFilingNum())
//                .prodCompany(createDTO.getProdCompany())
//                .crewDescription(createDTO.getCrewDescription())
//                .dramaDescription(createDTO.getDramaDescription())
//                .cast(createDTO.getCast())
//                .shootLocation(createDTO.getShootLocation())
//                .locationId(createDTO.getLocationId())
//                .service(createDTO.getService())
//                .serviceId(createDTO.getServiceId())
//                .userId(currentUser.getId())
//                .build();
//
//        this.save(drama);
//        return toDramaVO(drama);
//    }

    @Override
    public DramaVO updateDramaByAdmin(Long dramaId, UpdateDramaDTO updateDTO) {
        Drama drama = this.getById(dramaId);
        if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), DramaConstants.NOT_FIND);
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
                throw new NotFoundException(NOT_FOUND.code(), DramaConstants.NOT_FIND);
            }
        }
        cache.invalidate(CaffeineConstants.DRAMA+dramaId);
    }

//    @Override
//    public Page<DramaVO> getDramaPageByAdmin(Page<Drama> page, String keyword) {
//        QueryWrapper<Drama> wrapper = new QueryWrapper<>();
//
//        wrapper.select("id", "name", "drama_description", "cast", "cover");
//
//        if (keyword != null && !keyword.isEmpty()) {
//            wrapper.and(w -> w.like("name", keyword));
//        }
//
//        wrapper.orderByDesc("created_at");
//
//        Page<Drama> dramaPage = this.page(page, wrapper);
//
//        List<DramaVO> voList = dramaPage.getRecords().stream()
//                .map(drama -> new DramaVO(
//                        drama.getId(),
//                        drama.getName(),
//                        drama.getDramaDescription(),
//                        drama.getCast(),
//                        drama.getCover()
//                ))
//                .collect(Collectors.toList());
//
//        Page<DramaVO> voPage = new Page<>(dramaPage.getCurrent(), dramaPage.getSize(), dramaPage.getTotal());
//        voPage.setRecords(voList);
//
//        return voPage;
//    }

//    @Override
//    public DramaVO getDramaByIdByAdmin(Long dramaId) {
//        Object store = cache.asMap().get(CaffeineConstants.DRAMA + dramaId);
//        if (store != null) {
//            return toDramaVO((Drama) store);
//        }else {
//            Drama drama = this.getById(dramaId);
//            if (drama == null || Boolean.TRUE.equals(drama.getDeleted())) {
//                throw new NotFoundException(NOT_FOUND.code(), "电视剧备案不存在");
//            }
//            cache.asMap().put(CaffeineConstants.DRAMA + dramaId, dramaId);
//            return toDramaVO(drama);
//        }
//    }

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
