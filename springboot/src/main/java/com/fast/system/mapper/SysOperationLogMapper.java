package com.fast.system.mapper;

import com.fast.system.domain.SysOperationLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysOperationLogMapper {
    /**
     * 新增日志
     * @param log
     * @return
     */
    int insertLog(SysOperationLog log);
    /**
     * 查询操作日志列表。
     * 参数就是实体本身用 @Param 反而要写 6 个，不如直接传对象。
     */
    List<SysOperationLog> selectOperationLogList(SysOperationLog log);
}