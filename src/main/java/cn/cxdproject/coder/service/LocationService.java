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
    
    LocationVO createLocationByAdmin(Long userId, CreateLocationDTO createDTO);

    LocationVO getLocationById(Long locationId);

    List<LocationVO> getLocationPage(Long lastId,int size, String keyword);

    LocationVO updateLocationByAdmin(Long locationId, UpdateLocationDTO updateDTO);

    void deleteLocationByAdmin(Long locationId);
    
    LocationVO toLocationVO(Location location);

    LocationVO getByIdFallback(Long id,Throwable e);

    List<LocationVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    Page<LocationVO> getLocationPageAdmin(Page<Location> page, String keyword);
}
