package com.fast.system.mapper;

import com.fast.system.domain.SysPermissionLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysPermissionLogMapper {

    /**
     * 插入权限变更审计日志
     */
    int insertPermissionLog(SysPermissionLog log);

    /**
     * 查询权限变更日志列表
     */
    List<SysPermissionLog> selectPermissionLogList(SysPermissionLog log);
}