package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Day;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Day Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface DayMapper extends BaseMapper<Day> {

    /**
     * 根据 tour_id 查询所有 Day（未删除）
     */
    List<Day> selectByTourId(@Param("tourId") Long tourId);

    /**
     * 根据 tour_id 批量逻辑删除
     */
    int logicDeleteByTourId(@Param("tourId") Long tourId);
}
