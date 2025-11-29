package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.DramaVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.ShootVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Shoot 服务接口
 * @author Hibiscus-code-generate
 */
public interface ShootService extends IService<Shoot> {
    
    ShootVO createShootByAdmin(Long userId, CreateShootDTO createDTO);
    ShootVO getShootById(Long shootId);
    List<ShootVO> getShootPage(Long lastId, int size, String keyword);
    ShootVO updateShootByAdmin(Long shootId, UpdateShootDTO updateDTO);
    void deleteShootByAdmin(Long shootId);
    ShootVO toShootVO(Shoot shoot);
    ShootVO getByIdFallback(Long id,Throwable e);
    List<ShootVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    Page<ShootVO> getShootPageAdmin(Page<Shoot> page, String keyword);
}
