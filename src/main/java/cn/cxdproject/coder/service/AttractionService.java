package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateAttractionDTO;
import cn.cxdproject.coder.model.dto.UpdateAttractionDTO;
import cn.cxdproject.coder.model.entity.Attraction;
import cn.cxdproject.coder.model.vo.AttractionVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * Attraction 服务接口
 * 提供景点的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface AttractionService extends IService<Attraction> {

    /**
     * 根据 day_id 查询所有 Attraction
     */
    List<Attraction> listByDayId(Long dayId);

    /**
     * 根据 day_id 列表批量查询，返回 Map<dayId, List<Attraction>>
     */
    Map<Long, List<Attraction>> mapByDayIds(List<Long> dayIds);

    /**
     * 将 Attraction 实体转换为 AttractionVO
     */
    AttractionVO toAttractionVO(Attraction attraction);

    /**
     * 批量创建 Attractions
     */
    void batchCreateAttractions(Long tourId, Long dayId, List<CreateAttractionDTO> attractionDTOs);

    /**
     * 批量更新 Attractions
     */
    void batchUpdateAttractions(Long tourId, Long dayId, List<UpdateAttractionDTO> attractionDTOs);

    /**
     * 根据 day_id 逻辑删除所有关联的 Attractions
     */
    void logicDeleteByDayId(Long dayId);

    /**
     * 根据 tour_id 逻辑删除所有关联的 Attractions
     */
    void logicDeleteByTourId(Long tourId);
}
