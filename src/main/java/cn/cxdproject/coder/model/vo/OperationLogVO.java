package cn.cxdproject.coder.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogVO {

    private Long id;

    /**
     * 操作类型（如：LOGIN、DOC_EDIT）
     */
    private String type;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 执行用户ID
     */
    private Long userId;

    /**
     * 操作人标识（用户名或IP）
     */
    private String operator;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 输入参数快照
     */
    private String params;

    /**
     * 输出结果快照
     */
    private String result;

    /**
     * 操作时间
     */
    private LocalDateTime timestamp;

}
