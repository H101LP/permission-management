package com.fast.system.service;

import com.fast.system.domain.SysPermissionLog;

import java.util.List;

/**
 * 权限变更日志服务接口。
 * 本期只做查询透传。
 */
public interface ISysPermissionLogService {

    /** 查询权限变更日志列表 */
    List<SysPermissionLog> selectPermissionLogList(SysPermissionLog log);
}
