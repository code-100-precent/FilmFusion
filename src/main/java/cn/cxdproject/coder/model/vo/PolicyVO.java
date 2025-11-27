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

    private LocalDateTime issueTime;

    /**
     * 内容
     */

    private String content;

    /**
     * 封面
     */

    private String cover;

    /**
     * 图片
     */

    private String image;

    /**
     * 压缩封面
     */

    private String thumbCover;

    /**
     * 压缩图片
     */

    private String thumbImage;

    /**
     * created_at
     */

    private LocalDateTime createdAt;

    /**
     * updated_at
     */

    private LocalDateTime updatedAt;


}
