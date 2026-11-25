package com.fast.system.service.impl;

import com.fast.system.domain.SysPermissionLog;
import com.fast.system.mapper.SysPermissionLogMapper;
import com.fast.system.service.ISysPermissionLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 权限日志服务实现：纯透传。
 */
@Service
public class SysPermissionLogServiceImpl implements ISysPermissionLogService {

    @Resource
    private SysPermissionLogMapper permissionLogMapper;

    @Override
    public List<SysPermissionLog> selectPermissionLogList(SysPermissionLog log) {
        return permissionLogMapper.selectPermissionLogList(log);
    }
}
