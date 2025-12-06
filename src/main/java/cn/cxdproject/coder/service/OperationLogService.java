package cn.cxdproject.coder.service;

import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.OperationLogVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cn.cxdproject.coder.model.entity.OperationLog;

/**
 * OperationLog 服务接口
 * @author Hibiscus-code-generate
 */
public interface OperationLogService extends IService<OperationLog> {

    OperationLogVO getOperationById(Long id);

    void deleteArticle(Long id);

    Page<OperationLogVO> getOperationPage(Page<OperationLog> page, String keyword);

    OperationLogVO toOperationVO(OperationLog operationLog);
}
