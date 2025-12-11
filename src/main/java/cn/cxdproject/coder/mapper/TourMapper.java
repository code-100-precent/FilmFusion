package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.dto.UpdateTourDTO;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.entity.Shoot;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Tour;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Tour Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface TourMapper extends BaseMapper<Tour> {

    List<Long> selectIds(@Param("lastId") Long lastId,
                         @Param("size") int size,
                         @Param("keyword") String keyword);

    List<Tour> getAdminPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    long countAdminPage(@Param("keyword") String keyword);

    List<Tour> selectLatest10();

    int updateTour(
            @Param("id") Long id,
            @Param("dto") UpdateTourDTO dto
    );

    Long getTotal(@Param("keyword") String keyword);

}