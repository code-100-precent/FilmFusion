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
 * 提供剧组报备信息的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface ShootService extends IService<Shoot> {
    
    /**
     * 管理员创建拍摄报备
     */
    ShootVO createShootByAdmin(Long userId, CreateShootDTO createDTO);

    /**
     * 根据ID获取拍摄报备详情
     */
    ShootVO getShootById(Long shootId) throws InterruptedException;

    /**
     * 根据ID获取拍摄报备详情（带超时控制和降级）
     */
    ShootVO getShootByIdWithTimeout(Long shootId);

    /**
     * 分页获取拍摄报备列表（按时间倒序，公开接口）
     */
    List<ShootVO> getShootPage(Long lastId, int size, String keyword) throws InterruptedException;

    /**
     * 分页获取拍摄报备列表（带超时控制和降级）
     */
    List<ShootVO> getShootPageWithTimeout(Long lastId, int size, String keyword);

    /**
     * 管理员更新拍摄报备信息
     */
    ShootVO updateShootByAdmin(Long shootId, UpdateShootDTO updateDTO);

    /**
     * 管理员删除拍摄报备（逻辑删除）
     */
    void deleteShootByAdmin(Long shootId);

    /**
     * 将Shoot实体转换为ShootVO
     */
    ShootVO toShootVO(Shoot shoot);

    /**
     * 获取报备详情的降级方法
     */
    ShootVO getByIdFallback(Long id, Throwable e);

    /**
     * 分页查询报备列表的降级方法
     */
    List<ShootVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    /**
     * 管理员分页查询报备
     */
    Page<ShootVO> getShootPageAdmin(Page<Shoot> page, String keyword);
}
