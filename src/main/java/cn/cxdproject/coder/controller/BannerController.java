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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Banner 控制器，提供基础增删改查接口
 * @author Hibiscus-code-generate
 */
@RestController
@RequestMapping("/api/banner")
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    /**
     * 新增 Banner 记录
     * @param entity 实体对象
     * @return 是否新增成功
     */
    @GetMapping("/{id}")
    @PublicAccess
    public ApiResponse<BannerVO> getImageById(@PathVariable @NotNull(message = "图片ID不能为空") Long id) {
        BannerVO bannerVo = bannerService.getImageById(id);
        return ApiResponse.success(bannerVo);
    }

    /**
     * 更新 Banner 记录
     * @param entity 实体对象（必须包含主键 ID）
     * @return 是否更新成功
     */
    @GetMapping("/page")
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

    @PostMapping
    public ApiResponse<BannerVO> createImage(@Valid @RequestBody CreateBannerDTO createDTO) {
        BannerVO bannerVo = bannerService.createImage(createDTO);
        return ApiResponse.success(bannerVo);
    }

    @PutMapping("/update/{id}")
    public ApiResponse<BannerVO> updateImage(
            @PathVariable @NotNull(message = "图片ID不能为空") Long id,
            @Valid @RequestBody UpdateBannerDTO updateDTO) {
        BannerVO bannerVO = bannerService.updateImage(id,updateDTO);
        return ApiResponse.success(bannerVO);
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> deleteImage(@PathVariable @NotNull(message = "图片ID不能为空") Long id) {
        bannerService.deleteImage(id);
        return ApiResponse.success();
    }
}
