package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Location;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Policy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Policy Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface PolicyMapper extends BaseMapper<Policy> {
    List<Long> getPagePolicyIds(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    /**
     * 获取总数量（用于分页 total）
     */
    long countByKeyword(@Param("keyword") String keyword);

    List<Policy> selectLatest10();

}