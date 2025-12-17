package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateBannerDTO;
import cn.cxdproject.coder.model.dto.UpdateBannerDTO;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.BannerVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.Banner;

import java.util.List;


public interface BannerService extends IService<Banner> {

    /**
     * 删除Banner
     */
    void deleteImage(Long id);

    /**
     * 更新Banner信息
     */
    BannerVO updateImage(Long id, UpdateBannerDTO updateDTO);

    /**
     * 创建Banner
     */
    BannerVO createImage(CreateBannerDTO createDTO);

    /**
     * 管理员分页查询Banner
     */
    Page<BannerVO> getImageAdminPage(Page<Banner> page, String keyword);

    /**
     * 获取首页展示用Banner列表
     */
    List<BannerVO> getImagePage();

    /**
     * 根据ID获取Banner详情
     */
    BannerVO getImageById(Long id);

    /**
     * 将Banner实体转换为BannerVO
     */
    BannerVO toBannerVO(Banner banner);
}
