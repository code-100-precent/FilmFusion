package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Attraction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Attraction Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface AttractionMapper extends BaseMapper<Attraction> {

    /**
     * 根据 day_id 查询所有 Attraction（未删除）
     */
    List<Attraction> selectByDayId(@Param("dayId") Long dayId);

    /**
     * 根据 tour_id 查询所有 Attraction（未删除）
     */
    List<Attraction> selectByTourId(@Param("tourId") Long tourId);

    /**
     * 根据 day_id 批量逻辑删除
     */
    int logicDeleteByDayId(@Param("dayId") Long dayId);

    /**
     * 根据 tour_id 批量逻辑删除
     */
    int logicDeleteByTourId(@Param("tourId") Long tourId);

    /**
     * 根据 day_id 列表批量查询
     */
    List<Attraction> selectByDayIds(@Param("dayIds") List<Long> dayIds);
}
