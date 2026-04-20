package com.shengwei.tushuguanli.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标记需要特定权限标识才能访问的接口
 *
 * 使用示例:
 * @RequirePermission("book:add")
 * @PostMapping("/add")
 * public Result<Void> addBook(...) { ... }
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /**
     * 所需的权限标识（对应 sys_menu.perms 字段）
     * 支持多个权限，满足其一即可（OR 逻辑）
     */
    String[] value() default {};
}
