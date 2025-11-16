package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.BannerConstants;
import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateBannerDTO;
import cn.cxdproject.coder.model.dto.UpdateBannerDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.BannerVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.mapper.BannerMapper;
import cn.cxdproject.coder.service.BannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.NOT_FOUND;

/**
 * Banner 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
@Slf4j
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    private final BannerMapper bannerMapper;

    public BannerServiceImpl(BannerMapper bannerMapper) {
        this.bannerMapper = bannerMapper;
    }

    @Override
    public void deleteImage(Long id) {
        boolean updated = bannerMapper.update(null,
                Wrappers.<Banner>lambdaUpdate()
                        .set(Banner::getDeleted, true)
                        .eq(Banner::getId, id)
                        .eq(Banner::getDeleted, false)
        ) > 0;

        if (!updated) {
            Banner banner = this.getById(id);
            if (banner == null || Boolean.TRUE.equals(banner.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), BannerConstants.NOT_FIND);
            }
        }

    }

    @Override
    public BannerVO updateImage(Long id,UpdateBannerDTO updateDTO) {
        Banner banner = this.getById(id);
        if (banner == null || Boolean.TRUE.equals(banner.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), BannerConstants.NOT_FIND);
        }

        if (updateDTO.getImageName() != null) {
            banner.setImageName(updateDTO.getImageName());
        }
        if (updateDTO.getImageUrl() != null) {
            banner.setImageUrl(updateDTO.getImageUrl());
        }
        if (updateDTO.getSort() != null) {
            banner.setSort(updateDTO.getSort());
        }
        if (updateDTO.getStatus() != null) {
            banner.setStatus(updateDTO.getStatus());
        }

        banner.setUpdatedAt(LocalDateTime.now());
        this.updateById(banner);
        return toBannerVO(banner);
    }

    @Override
    public BannerVO createImage(CreateBannerDTO createDTO) {
        Banner banner = Banner.builder()
                .imageName(createDTO.getImageName())
                .imageUrl(createDTO.getImageUrl())
                .targetModule(createDTO.getTargetModule())
                .sort(createDTO.getSort())
                .status(true)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 保存文章
        this.save(banner);
        return toBannerVO(banner);
    }

    @Override
    public Page<BannerVO> getImagePage(Page<Banner> page, String keyword) {
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();

        wrapper.select("id", "image_name", "image_url", "target_module", "status", "sort")
                .eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("image_name", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Banner> imagePage = this.page(page, wrapper);

        List<BannerVO> voList = imagePage.getRecords().stream()
                .map(banner -> new BannerVO(
                        banner.getId(),
                        banner.getImageName(),
                        banner.getImageUrl(),
                        banner.getTargetModule(),
                        banner.getStatus(),
                        banner.getSort()
                ))
                .collect(Collectors.toList());

        Page<BannerVO> voPage = new Page<>(imagePage.getCurrent(), imagePage.getSize(), imagePage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public BannerVO getImageById(Long id) {
            Banner banner = this.getById(id);
            if (banner == null || Boolean.TRUE.equals(banner.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), BannerConstants.NOT_FIND);
            }
            return toBannerVO(banner);
    }

    @Override
    public BannerVO toBannerVO(Banner banner) {
        if (banner == null) {
            return null;
        }
        return BannerVO.builder()
                .id(banner.getId())
                .imageName(banner.getImageName())
                .sort(banner.getSort())
                .status(banner.getStatus())
                .targetModule(banner.getTargetModule())
                .imageUrl(banner.getImageUrl())
                .createdAt(banner.getCreatedAt())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }
}
