package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.CreateTourDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.ShootVO;
import cn.cxdproject.coder.model.vo.TourVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.Tour;

import java.util.List;

/**
 * Tour 服务接口
 * @author Hibiscus-code-generate
 */
public interface TourService extends IService<Tour> {
    TourVO createTourByAdmin(CreateTourDTO createDTO);
    TourVO getTourById(Long tourId);
    Page<TourVO> getTourPage(Page<Tour> page, String keyword);
    TourVO updateTourByAdmin(Long tourId, UpdateTourDTO updateDTO);
    void deleteTourByAdmin(Long tourId);
    TourVO toTourVO(Tour tour);
    TourVO getByIdFallback(Long id,Throwable e);
    Page<TourVO> getPageFallback(Page<Tour> page, String keyword, Throwable e);

}
