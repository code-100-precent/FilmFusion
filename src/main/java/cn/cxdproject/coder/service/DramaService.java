package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.DramaVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Drama 服务接口
 * @author Hibiscus-code-generate
 */
public interface DramaService extends IService<Drama> {
    
    /**
     * 创建电视剧备案（普通用户）
     */
    DramaVO createDrama(Long userId, CreateDramaDTO createDTO);
    
    /**
     * 更新电视剧备案（只能更新自己的）
     */
    DramaVO updateDrama(Long userId, Long dramaId, UpdateDramaDTO updateDTO);
    
    /**
     * 删除电视剧备案（只能删除自己的）
     */
    void deleteDrama(Long userId, Long dramaId);
    
    /**
     * 获取电视剧备案详情（公开）
     */
    DramaVO getDramaById(Long dramaId);
    
    /**
     * 分页获取电视剧备案列表（按时间倒序，公开）
     */
    Page<DramaVO> getDramaPage(Page<Drama> page, String keyword);
    
    /**
     * 管理员创建电视剧备案
     */
//    DramaVO createDramaByAdmin(CreateDramaDTO createDTO);
    
    /**
     * 管理员更新电视剧备案
     */
    DramaVO updateDramaByAdmin(Long dramaId, UpdateDramaDTO updateDTO);
    
    /**
     * 管理员删除电视剧备案
     */
    void deleteDramaByAdmin(Long dramaId);
    
    /**
     * 管理员分页查询电视剧备案
     */
//    Page<DramaVO> getDramaPageByAdmin(Page<Drama> page, String keyword);
    
    /**
     * 管理员获取电视剧备案详情
     */
//    DramaVO getDramaByIdByAdmin(Long dramaId);
    
    /**
     * 转换为VO
     */
    DramaVO toDramaVO(Drama drama);
}
