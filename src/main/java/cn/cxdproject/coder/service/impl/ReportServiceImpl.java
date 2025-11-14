package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.CaffeineConstants;
import cn.cxdproject.coder.common.context.AuthContext;
import cn.cxdproject.coder.exception.BusinessException;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.mapper.ArticleMapper;
import cn.cxdproject.coder.model.dto.CreateReportDTO;
import cn.cxdproject.coder.model.dto.UpdateReportDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.Report;
import cn.cxdproject.coder.model.entity.User;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.ReportVO;
import cn.cxdproject.coder.mapper.ReportMapper;
import cn.cxdproject.coder.service.ReportService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
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
                .status("未处理")
                .build();

        this.save(report);
        return toReportVO(report);
    }

    @Override
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
        if (updateDTO.getStatus() != null) throw new BusinessException(FORBIDDEN.code(), "无权修改申请状态");;

        cache.asMap().put(CaffeineConstants.REPORT + reportId, report);
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
                throw new NotFoundException(NOT_FOUND.code(), "申请不存在");
            } else {
                throw new BusinessException(FORBIDDEN.code(), "无权删除他人的申请");
            }
        }
        cache.invalidate(CaffeineConstants.REPORT+reportId);
    }

    @Override
    public ReportVO getReportById(Long reportId) {
        Object store = cache.asMap().get(CaffeineConstants.REPORT + reportId);
        if (store != null) {
            return toReportVO((Report) store);
        } else {
            Report report = this.getById(reportId);
            if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), "申请不存在");
            }
            cache.asMap().put(CaffeineConstants.REPORT + reportId, report);
            return toReportVO(report);
        }
    }

    @Override
    public Page<ReportVO> getReportPage(Page<Report> page, String keyword) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();

        wrapper.select("id", "user_id", "contact")
                .eq("deleted", false);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("contact", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Report> reportPage = this.page(page, wrapper);

        List<ReportVO> voList = reportPage.getRecords().stream()
                .map(report -> new ReportVO(
                        report.getId(),
                        report.getContact(),
                        report.getUserId()
                ))
                .collect(Collectors.toList());

        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public Page<ReportVO> getMyReportPage(Long userId, Page<Report> page) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("deleted", false);
        wrapper.orderByDesc("created_at");

        Page<Report> reportPage = this.page(page, wrapper);
        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        List<ReportVO> voList = reportPage.getRecords().stream()
                .map(this::toReportVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    //感觉没用
    @Override
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
    public ReportVO updateReportByAdmin(Long reportId, UpdateReportDTO updateDTO) {
        User currentUser = AuthContext.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException(UNAUTHORIZED.code(), "未登录");
        }
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

        cache.asMap().put(CaffeineConstants.REPORT + reportId, report);
        this.updateById(report);
        return toReportVO(report);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReportByAdmin(Long reportId) {
        boolean updated = reportMapper.update(null,
                Wrappers.<Report>lambdaUpdate()
                        .set(Report::getDeleted, true)
                        .eq(Report::getId, reportId)
                        .eq(Report::getDeleted, false)
        ) > 0;

        if (!updated) {
            Report report = this.getById(reportId);
            if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), "申请不存在或已删除");
            }
        }
        cache.invalidate(CaffeineConstants.REPORT+reportId);
    }

    @Override
    public Page<ReportVO> getReportPageByAdmin(Page<Report> page, String keyword) {
        QueryWrapper<Report> wrapper = new QueryWrapper<>();

        wrapper.select("id", "user_id", "contact");

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("contact", keyword));
        }

        wrapper.orderByDesc("created_at");

        Page<Report> reportPage = this.page(page, wrapper);

        List<ReportVO> voList = reportPage.getRecords().stream()
                .map(report -> new ReportVO(
                        report.getId(),
                        report.getContact(),
                        report.getUserId()
                ))
                .collect(Collectors.toList());

        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public ReportVO getReportByIdByAdmin(Long reportId) {
        Object store = cache.asMap().get(CaffeineConstants.REPORT + reportId);
        if (store != null) {
            return toReportVO((Report) store);
        } else {
            Report report = this.getById(reportId);
            if (report == null || Boolean.TRUE.equals(report.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), "申请不存在");
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
                .build();
    }
}
