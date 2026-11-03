
package com.fast.system.annotation;

import java.lang.annotation.*;

/**
 * 自定义权限校验注解
 */
@Target(ElementType.METHOD) // 只能贴在方法上
@Retention(RetentionPolicy.RUNTIME) // 运行时生效
@Documented
public @interface PreAuthorizePerm {
    /**
     * 权限标识，例如 "system:user:add"
     */
    String value();
}