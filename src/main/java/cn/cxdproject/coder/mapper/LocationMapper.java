package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.entity.Drama;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Location;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Location Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface LocationMapper extends BaseMapper<Location> {

    List<Long> selectIds(@Param("lastId") Long lastId,
                         @Param("size") int size,
                         @Param("keyword") String keyword);

    List<Location> getAdminPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    List<Location> selectLatest10();

    int updateLocation(
            @Param("id") Long id,
            @Param("dto") UpdateLocationDTO dto
    );

    Long getTotal(@Param("keyword") String keyword);
}