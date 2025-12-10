package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateBannerDTO;
import cn.cxdproject.coder.model.dto.UpdateBannerDTO;
import cn.cxdproject.coder.model.vo.BannerVO;
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
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }

    }

    @Override
    public BannerVO updateImage(Long id,UpdateBannerDTO updateDTO) {
        Banner banner = this.getById(id);
        if (banner == null || Boolean.TRUE.equals(banner.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
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
                .build();

        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());

        this.save(banner);
        return toBannerVO(banner);
    }

    @Override
    public Page<BannerVO> getImagePage(Page<Banner> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;


        List<Banner> banners = bannerMapper.getPage(keyword, offset, size);

        List<BannerVO> voList = banners.stream()
                .map(this::toBannerVO)
                .collect(Collectors.toList());

        return new Page<BannerVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList);
    }

    @Override
    public BannerVO getImageById(Long id) {
            Banner banner = this.getById(id);
            if (banner == null || Boolean.TRUE.equals(banner.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
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
