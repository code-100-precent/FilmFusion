package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Banner;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Drama;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Drama Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface DramaMapper extends BaseMapper<Drama> {

    List<Drama> getPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

}