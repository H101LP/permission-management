package com.fast.system.service;

import com.fast.system.domain.SysOperationLog;

public interface IAsyncLogService {
    /**
     * 异步保存操作日志
     */
    void saveLog(SysOperationLog logEntity);
}
