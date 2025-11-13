package cn.cxdproject.coder.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 分页参数
 *
 * @author heathcetide
 */
@Data
public class PageRequest {
    /**
     * 当前页码（从 1 开始）
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;

    /**
     * 排序字段，例如：createTime、name
     */
    private String sortBy;

    /**
     * 排序方式：asc / desc
     */
    private String sortOrder = "desc";

    /**
     * 模糊搜索关键字（可选）
     */
    private String keyword;

    /**
     * 精确过滤字段，例如 status=active, role=admin
     */
    private Map<String, Object> filters;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") // 让 JSON -> LocalDateTime
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 是否启用分页（有些接口可返回全部）
     */
    private Boolean pagination = true;

    /**
     * 是否统计总数（性能优化时可选择关闭）
     */
    private Boolean countTotal = true;
}