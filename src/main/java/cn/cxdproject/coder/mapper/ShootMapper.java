package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Location;
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

    List<Long> selectIds(@Param("lastId") Long lastId,
                         @Param("size") int size,
                         @Param("keyword") String keyword);

    List<Shoot> getAdminPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    List<Shoot> selectLatest10();

    int updateShoot(
            @Param("id") Long id,
            @Param("dto") UpdateShootDTO dto
    );

}