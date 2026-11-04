package com.fast.system.mapper;

import com.fast.system.domain.SysPermissionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysPermissionLogMapper {

    /**
     * 插入权限变更审计日志
     */
    int insertPermissionLog(SysPermissionLog log);
}