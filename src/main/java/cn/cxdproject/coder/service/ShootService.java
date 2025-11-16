package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateShootDTO;
import cn.cxdproject.coder.model.dto.UpdateShootDTO;
import cn.cxdproject.coder.model.entity.Shoot;
import cn.cxdproject.coder.model.vo.ShootVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Shoot 服务接口
 * @author Hibiscus-code-generate
 */
public interface ShootService extends IService<Shoot> {
    
    ShootVO createShoot(Long userId, CreateShootDTO createDTO);
    ShootVO updateShoot(Long userId, Long shootId, UpdateShootDTO updateDTO);
    void deleteShoot(Long userId, Long shootId);
    ShootVO getShootById(Long shootId);
    Page<ShootVO> getShootPage(Page<Shoot> page, String keyword);
    ShootVO updateShootByAdmin(Long shootId, UpdateShootDTO updateDTO);
    void deleteShootByAdmin(Long shootId);
    ShootVO toShootVO(Shoot shoot);
}
