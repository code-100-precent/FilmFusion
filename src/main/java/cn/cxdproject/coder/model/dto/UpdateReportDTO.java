package cn.cxdproject.coder.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 更新影视剧备案DTO
 * 
 * @author heathcetide
 */
@Data
public class UpdateReportDTO {
    
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
    private String status;
}

