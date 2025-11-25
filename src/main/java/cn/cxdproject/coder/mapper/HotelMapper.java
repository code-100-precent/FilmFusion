package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Hotel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
    List<Long> getPageHotelIds(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    /**
     * 获取总数量（用于分页 total）
     */
    long countByKeyword(@Param("keyword") String keyword);

    List<Hotel> selectLatest10();
}
