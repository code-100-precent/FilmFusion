package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateReportDTO;
import cn.cxdproject.coder.model.dto.UpdateReportAdminDTO;
import cn.cxdproject.coder.model.dto.UpdateReportUserDTO;
import cn.cxdproject.coder.model.entity.Report;
import cn.cxdproject.coder.model.vo.ReportVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Report 服务接口
 * 提供剧目报备申请的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface ReportService extends IService<Report> {
    
    /**
     * 用户创建报备
     */
    ReportVO createReport(Long userId, CreateReportDTO createDTO);

    /**
     * 用户更新自己的报备
     */
    ReportVO updateReport(Long userId, Long reportId, UpdateReportUserDTO updateDTO);

    /**
     * 用户删除自己的报备（逻辑删除）
     */
    void deleteReport(Long userId, Long reportId);

    /**
     * 获取报备详情（校验归属用户）
     */
    ReportVO getReportById(Long reportId, Long userId);

    /**
     * 分页获取当前用户的报备列表
     */
    Page<ReportVO> getMyReportPage(Long userId, Page<Report> page);

    /**
     * 管理员更新报备状态
     */
    ReportVO updateReportByAdmin(Long reportId, UpdateReportAdminDTO updateDTO);

    /**
     * 管理员删除报备（逻辑删除）
     */
    void deleteReportByAdmin(Long reportId);

    /**
     * 管理员分页查询报备
     */
    Page<ReportVO> getReportPageByAdmin(Page<Report> page, String keyword);

    /**
     * 管理员根据ID获取报备详情
     */
    ReportVO getReportByIdByAdmin(Long reportId);

    /**
     * 将Report实体转换为ReportVO
     */
    ReportVO toReportVO(Report report);
}
