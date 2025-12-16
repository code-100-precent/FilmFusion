package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Location 服务接口
 * 提供拍摄地的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface LocationService extends IService<Location> {
    
    /**
     * 管理员创建拍摄地
     */
    LocationVO createLocationByAdmin(Long userId, CreateLocationDTO createDTO);

    /**
     * 根据ID获取拍摄地详情
     */
    LocationVO getLocationById(Long locationId);

    /**
     * 分页获取拍摄地列表（按时间倒序，公开接口）
     */
    List<LocationVO> getLocationPage(Long lastId, int size, String keyword);

    /**
     * 管理员更新拍摄地信息
     */
    LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO);

    /**
     * 管理员删除拍摄地（逻辑删除）
     */
    void deleteLocationByAdmin(Long locationId);
    
    /**
     * 将Location实体转换为LocationVO
     */
    LocationVO toLocationVO(Location location);

    /**
     * 获取拍摄地详情的降级方法
     */
    LocationVO getByIdFallback(Long id, Throwable e);

    /**
     * 分页查询拍摄地列表的降级方法
     */
    List<LocationVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    /**
     * 管理员分页查询拍摄地
     */
    Page<LocationVO> getLocationPageAdmin(Page<Location> page, String keyword);
}
