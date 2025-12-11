package cn.cxdproject.coder.model.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PolicyVO {


    private Long id;

    /**
     * 政策标题
     */

    private String title;

    /**
     * 政策类型："省级" | "市级"
     */

    private String type;

    /**
     * 发布单位
     */

    private String issueUnit;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime issueTime;

    /**
     * 内容
     */

    private String content;

    /**
     * 图片
     */

    private String image;

    /**
     * 压缩图片
     */
    private String thumbImage;

    /**
     * 封面图片（与image相同，用于前端兼容）
     */
    private String cover;

    /**
     * 缩略封面（与thumbImage相同，用于前端兼容）
     */
    private String thumbCover;

    /**
     * created_at
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * updated_at
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private String status;


}
