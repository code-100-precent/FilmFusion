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
 * @author Hibiscus-code-generate
 */
public interface LocationService extends IService<Location> {
    
    LocationVO createLocation(Long userId, CreateLocationDTO createDTO);
    
    LocationVO updateLocation(Long userId, Long locationId, UpdateLocationDTO updateDTO);

    void deleteLocation(Long userId, Long locationId);

    LocationVO getLocationById(Long locationId);

    Page<LocationVO> getLocationPage(Page<Location> page, String keyword);

    LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO);

    void deleteLocationByAdmin(Long locationId);
    
    LocationVO toLocationVO(Location location);

    LocationVO getByIdFallBack(Long id);

    List<LocationVO> getPageFallback(Page<Location> page, String keyword, Throwable e);
}
