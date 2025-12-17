package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.CreateTourDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.model.vo.TourVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.Tour;

import java.util.List;

/**
 * Tour 服务接口
 * 提供线路产品的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface TourService extends IService<Tour> {

    /**
     * 管理员创建线路
     */
    TourVO createTourByAdmin(CreateTourDTO createDTO);

    /**
     * 根据ID获取线路详情
     */
    TourVO getTourById(Long tourId);

    /**
     * 根据ID获取线路详情（带超时控制和降级）
     */
    TourVO getTourByIdWithTimeout(Long tourId);

    /**
     * 分页获取线路列表（按时间倒序，公开接口）
     */
    List<TourVO> getTourPage(Long lastId, int size, String keyword);

    /**
     * 分页获取线路列表（带超时控制和降级）
     */
    List<TourVO> getTourPageWithTimeout(Long lastId, int size, String keyword);

    /**
     * 管理员更新线路信息
     */
    TourVO updateTourByAdmin(Long tourId, UpdateTourDTO updateDTO);

    /**
     * 管理员删除线路（逻辑删除）
     */
    void deleteTourByAdmin(Long tourId);

    /**
     * 将Tour实体转换为TourVO
     */
    TourVO toTourVO(Tour tour);

    /**
     * 获取线路详情的降级方法
     */
    TourVO getByIdFallback(Long id, Throwable e);

    /**
     * 分页查询线路列表的降级方法
     */
    List<TourVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    /**
     * 管理员分页查询线路
     */
    Page<TourVO> getTourPageAdmin(Page<Tour> page, String keyword);
}
