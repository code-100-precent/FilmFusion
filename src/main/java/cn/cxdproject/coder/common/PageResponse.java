package cn.cxdproject.coder.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应封装类，继承自 ApiResponse，用于统一分页格式。
 *
 * @author heathcetide
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends ApiResponse<List<T>> {

    private Pagination pagination;

    /**
     * 根据 MyBatis-Plus 的分页对象构建响应
     */
    public static <T> PageResponse<T> of(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setData(page.getRecords()); // 替换 getContent -> getRecords
        response.setPagination(new Pagination(
                (int) page.getCurrent(),          // 当前页
                (int) page.getSize(),             // 每页条数
                page.getTotal(),                  // 总条数
                (int) page.getPages()             // 总页数
        ));
        return response;
    }

    public static <T> PageResponse<T> of(int current, int size, long total, List<T> records) {
        PageResponse<T> response = new PageResponse<>();
        response.setData(records);
        response.setPagination(new Pagination(current, size, total,
                (int) ((total + size - 1) / size)));
        return response;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Pagination {
        private int currentPage;
        private int pageSize;
        private long totalItems;
        private int totalPages;
    }
}
