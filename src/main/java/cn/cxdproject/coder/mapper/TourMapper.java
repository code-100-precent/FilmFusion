package cn.cxdproject.coder.mapper;

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

    List<Long> getPageTourIds(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    /**
     * 获取总数量（用于分页 total）
     */
    long countByKeyword(@Param("keyword") String keyword);

    List<Tour> selectLatest10();

}