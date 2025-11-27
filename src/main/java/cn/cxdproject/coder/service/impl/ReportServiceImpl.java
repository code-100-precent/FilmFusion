package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.dto.CreateReportDTO;
import cn.cxdproject.coder.model.dto.UpdateReportDTO;
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
                .thumbImage(createDTO.getThumbImage())
                .build();

        this.save(report);
        return toReportVO(report);
    }

    @Override
    public ReportVO updateReport(Long userId, Long reportId, UpdateReportDTO updateDTO) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), ResponseConstants.NO_PERMISSION);
        }

        if (updateDTO.getName() != null) report.setName(updateDTO.getName());
        if (updateDTO.getType() != null) report.setType(updateDTO.getType());
        if (updateDTO.getGenre() != null) report.setGenre(updateDTO.getGenre());
        if (updateDTO.getEpisodes() != null) report.setEpisodes(updateDTO.getEpisodes());
        if (updateDTO.getInvestAmount() != null) report.setInvestAmount(updateDTO.getInvestAmount());
        if (updateDTO.getMainCreators() != null) report.setMainCreators(updateDTO.getMainCreators());
        if (updateDTO.getLeadProducer() != null) report.setLeadProducer(updateDTO.getLeadProducer());
        if (updateDTO.getProducerUnit() != null) report.setProducerUnit(updateDTO.getProducerUnit());
        if (updateDTO.getStartDate() != null) report.setStartDate(updateDTO.getStartDate());
        if (updateDTO.getEndDate() != null) report.setEndDate(updateDTO.getEndDate());
        if (updateDTO.getCrewScale() != null) report.setCrewScale(updateDTO.getCrewScale());
        if (updateDTO.getContact() != null) report.setContact(updateDTO.getContact());
        if (updateDTO.getPhoneNumber() != null) report.setPhoneNumber(updateDTO.getPhoneNumber());
        if (updateDTO.getCrewPosition() != null) report.setCrewPosition(updateDTO.getCrewPosition());
        if (updateDTO.getImage() != null) report.setImage(updateDTO.getImage());
        if (updateDTO.getStatus() != null) throw new BusinessException(FORBIDDEN.code(), "无权修改申请状态");;
        if (updateDTO.getThumbImage() != null) report.setThumbImage(updateDTO.getThumbImage());

        this.updateById(report);
        return toReportVO(report);
    }

    @Override
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
    public ReportVO updateReportByAdmin(Long reportId, UpdateReportDTO updateDTO) {
        User currentUser = AuthContext.getCurrentUser();
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }

        if (updateDTO.getStatus() != null) report.setStatus(updateDTO.getStatus());

        report.setUpdatedAt(LocalDateTime.now());
        cache.asMap().put(CaffeineConstants.REPORT + reportId, report);
        this.updateById(report);
        return toReportVO(report);
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
                .map(report -> new ReportVO(
                        report.getId(),
                        report.getContact(),
                        report.getUserId()
                ))
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
                .image(report.getImage())
                .thumbImage(report.getThumbImage())
                .build();
    }
}
