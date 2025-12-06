package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.dto.UpdateHotelDTO;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.entity.Hotel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
    List<Long> selectIds(@Param("lastId") Long lastId,
                         @Param("size") int size,
                         @Param("keyword") String keyword);

    List<Hotel> getAdminPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    List<Hotel> selectLatest10();

    int updateHotel(@Param("id") Long id,
                    @Param("dto") UpdateHotelDTO dto
    );
}
