package com.fast.system.service.impl;

import com.fast.system.domain.SysOperationLog;
import com.fast.system.mapper.SysOperationLogMapper;
import com.fast.system.service.ISysOperationLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志服务实现。
 * 纯透传：日志查询不需要校验、不需要拼装、不需要建树。
 */
@Service
public class SysOperationLogServiceImpl implements ISysOperationLogService {

    @Resource
    private SysOperationLogMapper operationLogMapper;

    /**
     * 查询操作日志列表。
     */
    @Override
    public List<SysOperationLog> selectOperationLogList(SysOperationLog log) {
        return operationLogMapper.selectOperationLogList(log);
    }
}
