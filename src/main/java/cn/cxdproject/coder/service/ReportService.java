package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateReportDTO;
import cn.cxdproject.coder.model.dto.UpdateReportDTO;
import cn.cxdproject.coder.model.entity.Report;
import cn.cxdproject.coder.model.vo.ReportVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Report 服务接口
 * @author Hibiscus-code-generate
 */
public interface ReportService extends IService<Report> {
    
    ReportVO createReport(Long userId, CreateReportDTO createDTO);
    ReportVO updateReport(Long userId, Long reportId, UpdateReportDTO updateDTO);
    void deleteReport(Long userId, Long reportId);
    ReportVO getReportById(Long reportId,Long userId);
//    Page<ReportVO> getReportPage(Page<Report> page, String keyword);
    Page<ReportVO> getMyReportPage(Long userId, Page<Report> page);
    
//    ReportVO createReportByAdmin(CreateReportDTO createDTO);
    ReportVO updateReportByAdmin(Long reportId, UpdateReportDTO updateDTO);
//    void deleteReportByAdmin(Long reportId);
    Page<ReportVO> getReportPageByAdmin(Page<Report> page, String keyword);
    ReportVO getReportByIdByAdmin(Long reportId);
    
    ReportVO toReportVO(Report report);
}
