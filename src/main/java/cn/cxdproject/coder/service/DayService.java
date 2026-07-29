package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateDayDTO;
import cn.cxdproject.coder.model.dto.UpdateDayDTO;
import cn.cxdproject.coder.model.entity.Day;
import cn.cxdproject.coder.model.vo.DayVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Day 服务接口
 * 提供每日行程的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface DayService extends IService<Day> {

    /**
     * 根据 tour_id 查询所有 Day
     */
    List<Day> listByTourId(Long tourId);

    /**
     * 将 Day 实体转换为 DayVO
     */
    DayVO toDayVO(Day day);

    /**
     * 批量创建 Days（包含关联的 Attractions）
     */
    void batchCreateDays(Long tourId, List<CreateDayDTO> dayDTOs);

    /**
     * 批量更新 Days（包含关联的 Attractions）
     */
    void batchUpdateDays(Long tourId, List<UpdateDayDTO> dayDTOs);

    /**
     * 根据 tour_id 逻辑删除所有关联的 Days
     */
    void logicDeleteByTourId(Long tourId);
}
