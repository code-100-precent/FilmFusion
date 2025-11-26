package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateHotelDTO;
import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateHotelDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Hotel;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.HotelVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface HotelService extends IService<Hotel> {
    HotelVO createHotelByAdmin(Long userId, CreateHotelDTO createDTO);

    HotelVO getHotelById(Long otelId);

    Page<HotelVO> getHotelPage(Page<Hotel> page, String keyword);

    HotelVO updateHotelByAdmin(Long hotelId, UpdateHotelDTO updateDTO);

    void deleteHotelByAdmin(Long hotelId);

    HotelVO toHotelVO(Hotel hotel);

    HotelVO getByIdFallback(Long id,Throwable e);

    Page<HotelVO> getPageFallback(Page<Hotel> page, String keyword, Throwable e);

}
