package cn.cxdproject.coder.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 反馈视图对象
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackVO {
    
    private Long id;
    private Long userId;
    private String content;
    private String status;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

