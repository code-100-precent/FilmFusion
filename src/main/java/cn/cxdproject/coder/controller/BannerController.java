package cn.cxdproject.coder.controller;

import cn.cxdproject.coder.common.ApiResponse;
import cn.cxdproject.coder.common.PageResponse;
import cn.cxdproject.coder.common.anno.PublicAccess;
import cn.cxdproject.coder.model.dto.CreateBannerDTO;
import cn.cxdproject.coder.model.dto.UpdateBannerDTO;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.vo.BannerVO;
import cn.cxdproject.coder.service.BannerService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Banner 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/banner")
@Slf4j
@Validated
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * 查找 Banner 记录
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<BannerVO> getImageById(@PathVariable @NotNull(message = "图片ID不能为空") Long id) {
        BannerVO bannerVo = bannerService.getImageById(id);
        return ApiResponse.success(bannerVo);
    }

    /**
     * 分页查询
     */
    @GetMapping("/admin/page")
    @PublicAccess
    public PageResponse<BannerVO> getImagePage(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<Banner> page = new Page<>(current, size);
        Page<BannerVO> imagePage = bannerService.getImagePage(page, keyword);

        return PageResponse.of(
                (int) imagePage.getCurrent(),
                (int) imagePage.getSize(),
                imagePage.getTotal(),
                imagePage.getRecords()
        );
    }

    /**
     * 新增图片
     */
    @PostMapping("/admin/create")
    public ApiResponse<BannerVO> createImage(@Valid @RequestBody CreateBannerDTO createDTO) {
        BannerVO bannerVo = bannerService.createImage(createDTO);
        return ApiResponse.success(bannerVo);
    }

    /**
     * 更新图片信息
     */
    @PutMapping("/admin/update/{id}")
    public ApiResponse<BannerVO> updateImage(
            @PathVariable @NotNull(message = "图片ID不能为空") Long id,
            @Valid @RequestBody UpdateBannerDTO updateDTO) {
        BannerVO bannerVO = bannerService.updateImage(id,updateDTO);
        return ApiResponse.success(bannerVO);
    }

    /**
     * 删除
     */
    @DeleteMapping("/admin/delete/{id}")
    public ApiResponse<Void> deleteImage(@PathVariable @NotNull(message = "图片ID不能为空") Long id) {
        bannerService.deleteImage(id);
        return ApiResponse.success();
    }
}
