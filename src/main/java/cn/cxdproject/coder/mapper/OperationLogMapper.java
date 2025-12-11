package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Banner;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * OperationLog Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    List<OperationLog> getPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    Long getTotal(@Param("keyword") String keyword);

}