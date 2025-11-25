package cn.cxdproject.coder.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String crewScale;
    private String contact;
    private String phoneNumber;
    private String crewPosition;
    private Long userId;
    private String status;
    private String image;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public ReportVO(Long id, String contact, Long userId) {
        this.id=id;
        this.contact=contact;
        this.userId=userId;
    }
}

