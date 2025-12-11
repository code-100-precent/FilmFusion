package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.entity.Article;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Banner;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Banner Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface BannerMapper extends BaseMapper<Banner> {

    List<Banner> getPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    Long getTotal(@Param("keyword") String keyword);

}