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

public interface HotelService extends IService<Hotel> {
    HotelVO createHotelByAdmin(Long userId, CreateHotelDTO createDTO);

    HotelVO getHotelById(Long otelId);

    List<HotelVO> getHotelPage(Long lastId, int size, String keyword);

    HotelVO updateHotelByAdmin(Long hotelId, UpdateHotelDTO updateDTO);

    void deleteHotelByAdmin(Long hotelId);

    HotelVO toHotelVO(Hotel hotel);

    HotelVO getByIdFallback(Long id,Throwable e);

    List<HotelVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    Page<HotelVO> getHotelPageAdmin(Page<Hotel> page, String keyword);
}
