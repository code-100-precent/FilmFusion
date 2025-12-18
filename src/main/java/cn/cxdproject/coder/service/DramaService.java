package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.dto.CreateDramaDTO;
import cn.cxdproject.coder.model.dto.UpdateDramaDTO;
import cn.cxdproject.coder.model.entity.Article;
import cn.cxdproject.coder.model.entity.Drama;
import cn.cxdproject.coder.model.vo.ArticleVO;
import cn.cxdproject.coder.model.vo.DramaVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * Drama 服务接口
 * 提供剧目备案的创建、查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface DramaService extends IService<Drama> {
    /**
     * 创建电视剧备案（只有管理员能进行）
     */
    DramaVO createDramaByAdmin(Long userId, CreateDramaDTO createDTO);
    
    /**
     * 获取电视剧备案详情（公开）
     */
    DramaVO getDramaById(Long dramaId) throws InterruptedException;
    
    /**
     * 获取电视剧备案详情（带超时控制和降级）
     */
    DramaVO getDramaByIdWithTimeout(Long dramaId);
    
    /**
     * 分页获取电视剧备案列表（按时间倒序，公开）
     */
    List<DramaVO> getDramaPage(Long lastId, int size, String keyword) throws InterruptedException;
    
    /**
     * 分页获取电视剧备案列表（带超时控制和降级）
     */
    List<DramaVO> getDramaPageWithTimeout(Long lastId, int size, String keyword);
    
    /**
     * 管理员更新电视剧备案
     */
    DramaVO updateDramaByAdmin(Long dramaId, UpdateDramaDTO updateDTO);
    
    /**
     * 管理员删除电视剧备案
     */
    void deleteDramaByAdmin(Long dramaId);

    /**
     * 转换为VO
     */
    DramaVO toDramaVO(Drama drama);

    /**
     * 获取剧目详情的降级方法
     */
    DramaVO getByIdFallback(Long id, Throwable e);

    /**
     * 分页查询剧目列表的降级方法
     */
    List<DramaVO> getPageFallback(Long lastId, int size, String keyword, Throwable e);

    /**
     * 管理员分页查询剧目备案
     */
    Page<DramaVO> getDramaPageAdmin(Page<Drama> page, String keyword);
}
