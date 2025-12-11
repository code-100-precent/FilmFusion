package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.anno.Loggable;
import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.common.enums.LogType;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateReportDTO;
import cn.cxdproject.coder.model.dto.UpdateReportAdminDTO;
import cn.cxdproject.coder.model.dto.UpdateReportUserDTO;
import cn.cxdproject.coder.model.entity.Report;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.ReportVO;
import cn.cxdproject.coder.mapper.ReportMapper;
import cn.cxdproject.coder.service.ReportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;


/**
 * Report 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    private final ReportMapper reportMapper;
    private final Cache<String, Object> cache;

    public ReportServiceImpl(
            ReportMapper reportMapper,
            @Qualifier("cache") Cache<String, Object> cache) {
        this.reportMapper = reportMapper;
        this.cache = cache;
    }

    @Override
    @Loggable(
            type = LogType.REPORT_USER_CREATE,
            value = "User submit report application"
    )
    public ReportVO createReport(Long userId, CreateReportDTO createDTO) {

        Report report = Report.builder()
                .name(createDTO.getName())
                .type(createDTO.getType())
                .genre(createDTO.getGenre())
                .episodes(createDTO.getEpisodes())
                .investAmount(createDTO.getInvestAmount())
                .mainCreators(createDTO.getMainCreators())
                .leadProducer(createDTO.getLeadProducer())
                .producerUnit(createDTO.getProducerUnit())
                .startDate(createDTO.getStartDate())
                .endDate(createDTO.getEndDate())
                .crewScale(createDTO.getCrewScale())
                .contact(createDTO.getContact())
                .phoneNumber(createDTO.getPhoneNumber())
                .crewPosition(createDTO.getCrewPosition())
                .userId(userId)
                .status(createDTO.getStatus())
                .shootPermit(createDTO.getShootPermit())
                .thumbShootPermit(createDTO.getThumbShootPermit())
                .shootApply(createDTO.getShootApply())
                .thumbShootApply(createDTO.getThumbShootApply())
                .approvalFile(createDTO.getApprovalFile())
                .thumbApprovalFile(createDTO.getThumbApprovalFile())
                .build();

        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        this.save(report);
        return toReportVO(report);
    }

    @Override
    @Loggable(
            type = LogType.REPORT_USER_UPDATE,
            value = "User update report application ID: #{#reportId}"
    )
    public ReportVO updateReport(Long userId, Long reportId, UpdateReportUserDTO updateDTO) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }
        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
        }

        int rows = reportMapper.updateReportByUser(reportId, updateDTO);
        if (rows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        Report updated = this.getById(reportId);
        cache.asMap().put(CaffeineConstants.REPORT + reportId, updated);
        return toReportVO(updated);
    }

    @Override
    @Loggable(
            type = LogType.REPORT_USER_DELETE,
            value = "User cancel report application ID: #{#reportId}"
    )
    public void deleteReport(Long userId, Long reportId) {
        boolean updated = reportMapper.update(null,
                Wrappers.<Report>lambdaUpdate()
                        .set(Report::getDeleted, true)
                        .eq(Report::getId, reportId)
                        .eq(Report::getUserId, userId)
                        .eq(Report::getDeleted, false)
        ) > 0;

        if (!updated) {
            Report report = this.getById(reportId);
            if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            } else {
                throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
            }
        }
        cache.invalidate(CaffeineConstants.REPORT+reportId);
    }

    @Override
    @Loggable(
            type = LogType.REPORT_USER_GET,
            value = "User get own report application by ID: #{#reportId}"
    )
    public ReportVO getReportById(Long reportId,Long userId) {
        Object store = cache.asMap().get(CaffeineConstants.REPORT + reportId);
        Report report;
        if (store != null) {
            report = (Report) store;
        } else {
            report = this.getById(reportId);
            if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.REPORT + reportId, report);
        }

        if (!userId.equals(report.getUserId())) {
            throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
        }
        return toReportVO(report);
    }


    @Override
    public Page<ReportVO> getMyReportPage(Long userId, Page<Report> page) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Report> reports = reportMapper.selectMyPage(userId, offset, size);

        long total = this.count(
                new LambdaQueryWrapper<Report>()
                        .eq(Report::getUserId, userId)
                        .eq(Report::getDeleted, false)
        );

        List<ReportVO> voList = reports.stream().map(this::toReportVO).collect(Collectors.toList());
        Page<ReportVO> voPage = new Page<>(current, size, total);
        voPage.setRecords(voList);
        return voPage;
    }


    @Override
    @Loggable(
            type = LogType.REPORT_ADMIN_UPDATE,
            value = "Admin process report application ID: #{#reportId}"
    )
    public ReportVO updateReportByAdmin(Long reportId, UpdateReportAdminDTO updateDTO) {
        if (updateDTO.getStatus() == null || updateDTO.getStatus().trim().isEmpty()) {
            throw new BusinessException(MISSING_PARAMETER.code(),"审核状态不能为空");
        }

        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        int rows = reportMapper.updateReportByAdmin(reportId, updateDTO.getStatus());
        if (rows == 0) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        Report updated = this.getById(reportId);
        cache.asMap().put(CaffeineConstants.REPORT + reportId, updated);
        return toReportVO(updated);
    }


    @Override
    public Page<ReportVO> getReportPageByAdmin(Page<Report> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        List<Report> reports = reportMapper.selectPageByAdmin(keyword, offset, size);

        LambdaQueryWrapper<Report> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Report::getDeleted, false);
        if (StringUtils.isNotBlank(keyword)) {
            countWrapper.like(Report::getContact, keyword.trim());
        }
        long total = this.count(countWrapper);

        List<ReportVO> voList = reports.stream()
                .map(this::toReportVO)
                .collect(Collectors.toList());

        return new Page<ReportVO>()
                .setCurrent(current)
                .setSize(size)
                .setTotal(total)
                .setRecords(voList);
    }

    @Override
    public ReportVO getReportByIdByAdmin(Long reportId) {
        Object store = cache.asMap().get(CaffeineConstants.REPORT + reportId);
        if (store != null) {
            return toReportVO((Report) store);
        } else {
            Report report = this.getById(reportId);
            if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
            cache.asMap().put(CaffeineConstants.REPORT + reportId, report);
            return toReportVO(report);
        }
    }

    @Override
    public ReportVO toReportVO(Report report) {
        if (report == null) {
            return null;
        }
        return ReportVO.builder()
                .id(report.getId())
                .name(report.getName())
                .type(report.getType())
                .genre(report.getGenre())
                .episodes(report.getEpisodes())
                .investAmount(report.getInvestAmount())
                .mainCreators(report.getMainCreators())
                .leadProducer(report.getLeadProducer())
                .producerUnit(report.getProducerUnit())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .crewScale(report.getCrewScale())
                .contact(report.getContact())
                .phoneNumber(report.getPhoneNumber())
                .crewPosition(report.getCrewPosition())
                .userId(report.getUserId())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .shootPermit(report.getShootPermit())
                .thumbShootPermit(report.getThumbShootPermit())
                .shootApply(report.getShootApply())
                .thumbShootApply(report.getThumbShootApply())
                .approvalFile(report.getApprovalFile())
                .thumbApprovalFile(report.getThumbApprovalFile())
                .build();
    }
}
