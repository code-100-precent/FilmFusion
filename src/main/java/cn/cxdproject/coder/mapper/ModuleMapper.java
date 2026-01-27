package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.dto.UpdateModuleDTO;
import cn.cxdproject.coder.model.entity.Module;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Module Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface ModuleMapper extends BaseMapper<Module> {

    List<Long> selectIds(@Param("lastId") Long lastId,
                         @Param("size") int size,
                         @Param("keyword") String keyword);

    List<Module> getAdminPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    int updateModule(
            @Param("id") Long id,
            @Param("dto") UpdateModuleDTO dto
    );

    Long getTotal(@Param("keyword") String keyword);

    List<Module> selectAll();
}
