package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.mapper.AttractionMapper;
import cn.cxdproject.coder.model.dto.CreateAttractionDTO;
import cn.cxdproject.coder.model.dto.UpdateAttractionDTO;
import cn.cxdproject.coder.model.entity.Attraction;
import cn.cxdproject.coder.model.vo.AttractionVO;
import cn.cxdproject.coder.service.AttractionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Attraction 服务实现类
 * @author Hibiscus-code-generate
 */
@Slf4j
@Service
public class AttractionServiceImpl extends ServiceImpl<AttractionMapper, Attraction> implements AttractionService {

    private final AttractionMapper attractionMapper;

    public AttractionServiceImpl(AttractionMapper attractionMapper) {
        this.attractionMapper = attractionMapper;
    }

    @Override
    public List<Attraction> listByDayId(Long dayId) {
        return attractionMapper.selectByDayId(dayId);
    }

    @Override
    public Map<Long, List<Attraction>> mapByDayIds(List<Long> dayIds) {
        if (dayIds == null || dayIds.isEmpty()) {
            return Map.of();
        }
        List<Attraction> attractions = attractionMapper.selectByDayIds(dayIds);
        return attractions.stream()
                .collect(Collectors.groupingBy(Attraction::getDayId));
    }

    @Override
    public AttractionVO toAttractionVO(Attraction attraction) {
        if (attraction == null) {
            return null;
        }
        return AttractionVO.builder()
                .id(attraction.getId())
                .dayId(attraction.getDayId())
                .tourId(attraction.getTourId())
                .name(attraction.getName())
                .highlights(attraction.getHighlights())
                .locationId(attraction.getLocationId())
                .dramaId(attraction.getDramaId())
                .hotelId(attraction.getHotelId())
                .image(attraction.getImage())
                .thumbImage(attraction.getThumbImage())
                .createdAt(attraction.getCreatedAt())
                .updatedAt(attraction.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchCreateAttractions(Long tourId, Long dayId, List<CreateAttractionDTO> attractionDTOs) {
        if (attractionDTOs == null || attractionDTOs.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        List<Attraction> attractions = attractionDTOs.stream()
                .map(dto -> {
                    Attraction attraction = Attraction.builder()
                            .tourId(tourId)
                            .dayId(dayId)
                            .name(dto.getName())
                            .highlights(dto.getHighlights())
                            .locationId(dto.getLocationId())
                            .dramaId(dto.getDramaId())
                            .hotelId(dto.getHotelId())
                            .image(dto.getImage())
                            .thumbImage(dto.getThumbImage())
                            .build();
                    attraction.setCreatedAt(now);
                    attraction.setUpdatedAt(now);
                    attraction.setDeleted(false);
                    return attraction;
                })
                .collect(Collectors.toList());

        this.saveBatch(attractions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateAttractions(Long tourId, Long dayId, List<UpdateAttractionDTO> attractionDTOs) {
        if (attractionDTOs == null || attractionDTOs.isEmpty()) {
            return;
        }

        // 先删除该 day 下的所有旧 attractions
        logicDeleteByDayId(dayId);

        // 重新创建（简化更新逻辑）
        LocalDateTime now = LocalDateTime.now();
        List<Attraction> attractions = attractionDTOs.stream()
                .map(dto -> {
                    Attraction attraction = Attraction.builder()
                            .tourId(tourId)
                            .dayId(dayId)
                            .name(dto.getName())
                            .highlights(dto.getHighlights())
                            .locationId(dto.getLocationId())
                            .dramaId(dto.getDramaId())
                            .hotelId(dto.getHotelId())
                            .image(dto.getImage())
                            .thumbImage(dto.getThumbImage())
                            .build();
                    attraction.setCreatedAt(now);
                    attraction.setUpdatedAt(now);
                    attraction.setDeleted(false);
                    return attraction;
                })
                .collect(Collectors.toList());

        this.saveBatch(attractions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDeleteByDayId(Long dayId) {
        attractionMapper.logicDeleteByDayId(dayId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logicDeleteByTourId(Long tourId) {
        attractionMapper.logicDeleteByTourId(tourId);
    }
}
