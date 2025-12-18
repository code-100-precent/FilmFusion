package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Article;
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

    List<Long> selectIds(@Param("lastId") Long lastId,
                         @Param("size") int size,
                         @Param("keyword") String keyword);

    List<Drama> getAdminPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );


    List<Drama> selectLatest10();

    int updateDrama(
            @Param("dramaId") Long dramaId,
            @Param("dto") UpdateDramaDTO dto
    );

    Long getTotal(@Param("keyword") String keyword);

    List<Drama> selectAll();
}