package com.fast.system.service;

import com.fast.system.domain.SysOperationLog;
import com.fast.system.domain.SysPermissionLog;

public interface IAsyncLogService {
    /**
     * 异步保存操作日志
     */
    void saveLog(SysOperationLog logEntity);
    /**
     * 异步保存权限变更日志
     */
    void savePermissionLog(SysPermissionLog log);
}
