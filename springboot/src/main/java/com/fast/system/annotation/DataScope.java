package com.fast.system.annotation;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {
    /**
     * 部门表别名
     */
    String deptAlias() default "u";

    /**
     * 用户表别名
     */
    String userAlias() default "u";
}