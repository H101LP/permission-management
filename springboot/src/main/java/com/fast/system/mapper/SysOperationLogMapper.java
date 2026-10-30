package com.fast.system.mapper;

import com.fast.system.domain.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysOperationLogMapper {
    int insertLog(SysOperationLog log);
}