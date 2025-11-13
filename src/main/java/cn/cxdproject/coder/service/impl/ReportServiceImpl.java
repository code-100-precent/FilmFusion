package cn.cxdproject.coder.service.impl;

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
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.*;

/**
 * Report 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Override
    @Transactional(rollbackFor = Exception.class)
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
                .build();

        this.save(report);
        return toReportVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO updateReport(Long userId, Long reportId, UpdateReportDTO updateDTO) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "影视剧备案不存在");
        }

        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权修改他人的影视剧备案");
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

        this.updateById(report);
        return toReportVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReport(Long userId, Long reportId) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "影视剧备案不存在");
        }

        if (!report.getUserId().equals(userId)) {
            throw new BusinessException(FORBIDDEN.code(), "无权删除他人的影视剧备案");
        }

        report.setDeleted(true);
        this.updateById(report);
    }

    @Override
    public ReportVO getReportById(Long reportId) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "影视剧备案不存在");
        }
        return toReportVO(report);
    }

    @Override
    public Page<ReportVO> getReportPage(Page<Report> page, String keyword) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("type", keyword).or().like("genre", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Report> reportPage = this.page(page, wrapper);
        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        List<ReportVO> voList = reportPage.getRecords().stream()
                .map(this::toReportVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO createReportByAdmin(CreateReportDTO createDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
        }

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
                .userId(currentUser.getId())
                .build();

        this.save(report);
        return toReportVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO updateReportByAdmin(Long reportId, UpdateReportDTO updateDTO) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "影视剧备案不存在");
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

        this.updateById(report);
        return toReportVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReportByAdmin(Long reportId) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "影视剧备案不存在");
        }

        report.setDeleted(true);
        this.updateById(report);
    }

    @Override
    public Page<ReportVO> getReportPageByAdmin(Page<Report> page, String keyword) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("type", keyword).or().like("genre", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Report> reportPage = this.page(page, wrapper);
        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        List<ReportVO> voList = reportPage.getRecords().stream()
                .map(this::toReportVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public ReportVO getReportByIdByAdmin(Long reportId) {
        Report report = this.getById(reportId);
        if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), "影视剧备案不存在");
        }
        return toReportVO(report);
    }

    @Override
    public ReportVO toReportVO(Report report) {
        if (report == null) {
            return null;
        }
        ReportVO vo = new ReportVO();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }
}
