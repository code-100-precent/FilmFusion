package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateBannerDTO;
import cn.cxdproject.coder.model.dto.UpdateBannerDTO;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.BannerVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.Banner;

import java.util.List;

/**
 * Banner 服务接口
 * @author Hibiscus-code-generate
 */
public interface BannerService extends IService<Banner> {

    void deleteImage(Long id);

    BannerVO updateImage(Long id,UpdateBannerDTO updateDTO);

    BannerVO createImage(CreateBannerDTO createDTO);

    Page<BannerVO> getImageAdminPage(Page<Banner> page, String keyword);

    List<BannerVO> getImagePage();

    BannerVO getImageById(Long id);

    BannerVO toBannerVO(Banner banner);
}
