package com.fast.system.annotation;


import com.fast.system.enums.OperType;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // 作用于方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Documented
public @interface OperationLog {
    String module();    // 模块名
    String business();  // 业务动作
    OperType type(); // 操作类型
}