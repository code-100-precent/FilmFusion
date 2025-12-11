package cn.cxdproject.coder.service.impl;

import cn.cxdproject.coder.common.constants.ResponseConstants;
import cn.cxdproject.coder.exception.NotFoundException;
import cn.cxdproject.coder.model.entity.Banner;
import cn.cxdproject.coder.model.entity.Location;
import cn.cxdproject.coder.model.vo.BannerVO;
import cn.cxdproject.coder.model.vo.LocationVO;
import cn.cxdproject.coder.model.vo.OperationLogVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.cxdproject.coder.model.entity.OperationLog;
import cn.cxdproject.coder.mapper.OperationLogMapper;
import cn.cxdproject.coder.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static cn.cxdproject.coder.common.enums.ResponseCodeEnum.NOT_FOUND;

/**
 * OperationLog 服务实现类
 * @author Hibiscus-code-generate
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    public OperationLogVO getOperationById(Long id) {
        OperationLog operationLog = this.getById(id);
        if (operationLog == null || Boolean.TRUE.equals(operationLog.getDeleted())) {
            throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
        }
        return toOperationVO(operationLog);
    }

    @Override
    public void deleteArticle(Long id) {
        boolean updated = operationLogMapper.update(null,
                Wrappers.<OperationLog>lambdaUpdate()
                        .set(OperationLog::getDeleted, true)
                        .eq(OperationLog::getId, id)
                        .eq(OperationLog::getDeleted, false)
        ) > 0;

        if (!updated) {
            OperationLog operationLog = this.getById(id);
            if (operationLog == null || Boolean.TRUE.equals(operationLog.getDeleted())) {
                throw new NotFoundException(NOT_FOUND.code(), ResponseConstants.NOT_FIND);
            }
        }

    }

    @Override
    public Page<OperationLogVO> getOperationPage(Page<OperationLog> page, String keyword) {
        long current = page.getCurrent();
        long size = page.getSize();
        long offset = (current - 1) * size;

        // 获取当前页的数据和总记录数
        List<OperationLog> operationLogs = operationLogMapper.getPage(keyword, offset, size);
        Long total = operationLogMapper.getTotal(keyword);

        List<OperationLogVO> voList = operationLogs.stream()
                .map(this::toOperationVO)
                .collect(Collectors.toList());

        return new Page<OperationLogVO>()
                .setCurrent(current)
                .setSize(size)
                .setRecords(voList)
                .setTotal(total);
    }

    @Override
    public OperationLogVO toOperationVO(OperationLog operationLog) {
        if (log == null) {
            return null;
        }
        return OperationLogVO.builder()
                .id(operationLog.getId())
                .type(operationLog.getType())
                .description(operationLog.getDescription())
                .userId(operationLog.getUserId())
                .userName(operationLog.getUserName())
                .operator(operationLog.getOperator())
                .success(operationLog.getSuccess())
                .params(operationLog.getParams())
                .result(operationLog.getResult())
                .timestamp(operationLog.getTimestamp())
                .build();
    }
}
