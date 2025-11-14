package cn.cxdproject.coder.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 影视剧备案视图对象
 * 
 * @author heathcetide
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportVO {
    
    private Long id;
    private String name;
    private String type;
    private String genre;
    private Integer episodes;
    private BigDecimal investAmount;
    private String mainCreators;
    private String leadProducer;
    private String producerUnit;
    private LocalDate startDate;
    private LocalDate endDate;
    private String crewScale;
    private String contact;
    private String phoneNumber;
    private String crewPosition;
    private Long userId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

