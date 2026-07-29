package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.mapper.DayMapper;
import cn.cxdproject.coder.model.dto.CreateDayDTO;
import cn.cxdproject.coder.model.dto.UpdateDayDTO;
import cn.cxdproject.coder.model.entity.Attraction;
import cn.cxdproject.coder.model.entity.Day;
import cn.cxdproject.coder.model.vo.AttractionVO;
import cn.cxdproject.coder.model.vo.DayVO;
import cn.cxdproject.coder.service.AttractionService;
import cn.cxdproject.coder.service.DayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Day 服务实现类
 * @author Hibiscus-code-generate
 */
@Slf4j
@Service
public class DayServiceImpl extends ServiceImpl<DayMapper, Day> implements DayService {

    private final DayMapper dayMapper;
    private final AttractionService attractionService;

    public DayServiceImpl(DayMapper dayMapper, AttractionService attractionService) {
        this.dayMapper = dayMapper;
        this.attractionService = attractionService;
    }

    @Override
    public List<Day> listByTourId(Long tourId) {
        return dayMapper.selectByTourId(tourId);
    }

    @Override
    public DayVO toDayVO(Day day) {
        if (day == null) {
            return null;
        }

        // 查询该 day 下的所有 attractions
        List<Attraction> attractions = attractionService.listByDayId(day.getId());
        List<AttractionVO> attractionVOs = attractions.stream()
                .map(attractionService::toAttractionVO)
                .collect(Collectors.toList());

        return DayVO.builder()
                .id(day.getId())
                .tourId(day.getTourId())
                .name(day.getName())
                .day(day.getDay())
                .attractions(attractionVOs)
                .createdAt(day.getCreatedAt())
                .updatedAt(day.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateDays(Long tourId, List<CreateDayDTO> dayDTOs) {
        if (dayDTOs == null || dayDTOs.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (CreateDayDTO dayDTO : dayDTOs) {
            // 1. 创建 Day
            Day day = Day.builder()
                    .tourId(tourId)
                    .name(dayDTO.getName())
                    .day(dayDTO.getDay())
                    .build();
            day.setCreatedAt(now);
            day.setUpdatedAt(now);
            day.setDeleted(false);

            this.save(day);

            // 2. 创建该 Day 下的所有 Attractions
            attractionService.batchCreateAttractions(tourId, day.getId(), dayDTO.getAttractions());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateDays(Long tourId, List<UpdateDayDTO> dayDTOs) {
        if (dayDTOs == null || dayDTOs.isEmpty()) {
            return;
        }

        // 先删除该 tour 下的所有旧数据
        logicDeleteByTourId(tourId);
        attractionService.logicDeleteByTourId(tourId);

        // 重新创建（简化更新逻辑）
        LocalDateTime now = LocalDateTime.now();

        for (UpdateDayDTO dayDTO : dayDTOs) {
            Day day = Day.builder()
                    .tourId(tourId)
                    .name(dayDTO.getName())
                    .day(dayDTO.getDay())
                    .build();
            day.setCreatedAt(now);
            day.setUpdatedAt(now);
            day.setDeleted(false);

            this.save(day);

            // 创建 Attractions
            if (dayDTO.getAttractions() != null && !dayDTO.getAttractions().isEmpty()) {
                attractionService.batchUpdateAttractions(tourId, day.getId(), dayDTO.getAttractions());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDeleteByTourId(Long tourId) {
        dayMapper.logicDeleteByTourId(tourId);
    }
}
