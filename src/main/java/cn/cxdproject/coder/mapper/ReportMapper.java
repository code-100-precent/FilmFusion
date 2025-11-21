package cn.cxdproject.coder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.cxdproject.coder.model.entity.Report;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Report Mapper 接口
 * @author Hibiscus-code-generate
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {


        List<Report> selectMyPage(
                @Param("userId") Long userId,
                @Param("offset") long offset,
                @Param("size") long size);

        List<Report> selectPageByAdmin(
                @Param("keyword") String keyword,
                @Param("offset") long offset,
                @Param("size") long size
        );


}