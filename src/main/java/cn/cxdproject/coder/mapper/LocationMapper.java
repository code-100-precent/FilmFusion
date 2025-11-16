package cn.cxdproject.coder.mapper;

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

    List<Location> getPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );
}