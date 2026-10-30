package com.fast.system.service.impl;

import com.fast.system.domain.SysOperationLog;
import com.fast.system.mapper.SysOperationLogMapper;
import com.fast.system.service.IAsyncLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncLogServiceImpl implements IAsyncLogService {

    @Resource
    private SysOperationLogMapper sysOperationLogMapper;

    // @Async 核心注解：扔给 Spring 线程池去执行
    @Override
    @Async
    public void saveLog(SysOperationLog logEntity) {
        try {
            sysOperationLogMapper.insertLog(logEntity);
        } catch (Exception e) {
            log.error("异步保存审计日志失败", e);
        }
    }
}