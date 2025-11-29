package cn.cxdproject.coder.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorPageResponseVO<T> {
    /** 当前页数据 */
    private List<T> records;
    /** 下一页游标（null 表示无更多数据） */
    private String nextCursor;
    /** 是否还有更多数据（前端直接使用，无需自己计算） */
    private boolean hasMore;

    // 便捷构造方法：自动计算 hasMore
    public CursorPageResponseVO(List<T> records, String nextCursor) {
        this.records = records;
        this.nextCursor = nextCursor;
        this.hasMore = nextCursor != null; // 游标不为空即有更多数据
    }

}
