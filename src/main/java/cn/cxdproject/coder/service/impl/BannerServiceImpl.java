package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateBannerDTO;
import cn.cxdproject.coder.model.dto.UpdateBannerDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.vo.ArticleVO;
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

    //管理员删除banner
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

    //管理员更新banner
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
        // 如果targetModule为空，设置为空字符串
        if (updateDTO.getTargetModule() != null) {
            String targetModule = updateDTO.getTargetModule().trim().isEmpty() ? "" : updateDTO.getTargetModule();
            banner.setTargetModule(targetModule);
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

    //管理员创建banner
    @Override
    public BannerVO createImage(CreateBannerDTO createDTO) {
        // 如果targetModule为空，设置为空字符串
        String targetModule = createDTO.getTargetModule();
        if (targetModule == null || targetModule.trim().isEmpty()) {
            targetModule = "";
        }
        
        Banner banner = Banner.builder()
                .imageName(createDTO.getImageName())
                .imageUrl(createDTO.getImageUrl())
                .targetModule(targetModule)
                .sort(createDTO.getSort())
                .status(true)
                .build();

        banner.setCreatedAt(LocalDateTime.now());
        banner.setUpdatedAt(LocalDateTime.now());

        this.save(banner);
        return toBannerVO(banner);
    }

    //管理员分页查询
    @Override
    public Page<BannerVO> getImageAdminPage(Page<Banner> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        // 获取当前页的数据和总记录数
        List<Banner> banners = bannerMapper.getAdminPage(keyword, offset, size);
        Long total = bannerMapper.getAdminTotal(keyword);

        List<BannerVO> voList = banners.stream()
                .map(this::toBannerVO)
                .collect(Collectors.toList());

        return new Page<BannerVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }

    //客户端返回所有可用banner数据
    @Override
    public List<BannerVO> getImagePage() {
        return bannerMapper.getPage().stream()
                .map(this::toBannerVO)
                .collect(Collectors.toList());

    }

    //根据id查询
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
