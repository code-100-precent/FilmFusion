package cn.cxdproject.coder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Article Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 延迟关联分页查询（
     */
    List<Article> getPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

}