package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateLocationDTO;
import cn.cxdproject.coder.model.dto.UpdateLocationDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.LocationVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Location 服务接口
 * @author Hibiscus-code-generate
 */
public interface LocationService extends IService<Location> {
    
    LocationVO createLocation(Long userId, CreateLocationDTO createDTO);
    LocationVO updateLocation(Long userId, Long locationId, UpdateLocationDTO updateDTO);
    void deleteLocation(Long userId, Long locationId);
    LocationVO getLocationById(Long locationId);
    Page<LocationVO> getLocationPage(Page<Location> page, String keyword);
    
    LocationVO createLocationByAdmin(CreateLocationDTO createDTO);
    LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO);
    void deleteLocationByAdmin(Long locationId);
    Page<LocationVO> getLocationPageByAdmin(Page<Location> page, String keyword);
    LocationVO getLocationByIdByAdmin(Long locationId);
    
    LocationVO toLocationVO(Location location);
}
