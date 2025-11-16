package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Drama;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Shoot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Shoot Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface ShootMapper extends BaseMapper<Shoot> {

    List<Shoot> getPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

}