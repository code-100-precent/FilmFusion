package cn.cxdproject.coder.mapper;

import cn.cxdproject.coder.model.dto.UpdateArticleDTO;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Tour;
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
     * 分页查询文章ID（用于后续缓存加载）
     */
    List<Long> selectIds(@Param("lastId") Long lastId,
                               @Param("size") int size,
                               @Param("keyword") String keyword);

    List<Article> getAdminPage(
            @Param("keyword") String keyword,
            @Param("offset") long offset,
            @Param("size") long size
    );

    List<Article> selectLatest10();

    int updateArticle(
            @Param("articleId") Long articleId,
            @Param("dto") UpdateArticleDTO updateArticleDTO
    );

    Long getTotal(@Param("keyword") String keyword);

    List<Article> selectAll();

}