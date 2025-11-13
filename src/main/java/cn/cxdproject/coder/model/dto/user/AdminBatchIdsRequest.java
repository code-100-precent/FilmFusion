package cn.cxdproject.coder.model.dto.user;

import lombok.Data;

import java.util.List;

@Data
public class AdminBatchIdsRequest {
    private List<Long> ids;
}