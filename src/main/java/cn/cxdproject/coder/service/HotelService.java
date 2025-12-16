package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateHotelDTO;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateHotelDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Hotel;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.HotelVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Hotel 服务接口
 * 定义酒店创建、查询及管理相关能力
 */
public interface HotelService extends IService<Hotel> {
    /**
     * 管理员创建酒店
     */
    HotelVO createHotelByAdmin(Long userId, CreateHotelDTO createDTO);

    /**
     * 根据ID获取酒店详情
     */
    HotelVO getHotelById(Long otelId);

    /**
     * 分页获取酒店列表（按时间倒序，公开接口）
     */
    List<HotelVO> getHotelPage(Long lastId, int size, String keyword);

    /**
     * 管理员更新酒店信息
     */
    HotelVO updateHotelByAdmin(Long hotelId, UpdateHotelDTO updateDTO);

    /**
     * 管理员删除酒店（逻辑删除）
     */
    void deleteHotelByAdmin(Long hotelId);

    /**
     * 将Hotel实体转换为HotelVO
     */
    HotelVO toHotelVO(Hotel hotel);

    /**
     * 获取酒店详情的降级方法
     */
    HotelVO getByIdFallback(Long id, Throwable e);

    /**
     * 分页查询酒店列表的降级方法
     */
    List<HotelVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    /**
     * 管理员分页查询酒店
     */
    Page<HotelVO> getHotelPageAdmin(Page<Hotel> page, String keyword);
}
