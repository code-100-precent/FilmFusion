package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.OperationLogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.OperationLog;

/**
 * OperationLog 服务接口
 * 提供操作日志的查询及管理能力
 * @author Hibiscus-code-generate
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 根据ID获取操作日志详情
     */
    OperationLogVO getOperationById(Long id);

    /**
     * 删除操作日志（逻辑删除）
     */
    void deleteArticle(Long id);

    /**
     * 分页查询操作日志
     */
    Page<OperationLogVO> getOperationPage(Page<OperationLog> page, String keyword);

    /**
     * 将OperationLog实体转换为OperationLogVO
     */
    OperationLogVO toOperationVO(OperationLog operationLog);
}
