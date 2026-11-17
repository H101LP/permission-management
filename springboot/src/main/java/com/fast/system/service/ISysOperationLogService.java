package com.fast.system.service;

import com.fast.system.domain.SysOperationLog;

import java.util.List;

/**
 * 操作日志服务接口。
 * 本期只做查询透传；后续"导出 / 90 天归档清理"也挂在这里。
 */
public interface ISysOperationLogService {

    /** 查询操作日志列表 */
    List<SysOperationLog> selectOperationLogList(SysOperationLog log);
}
