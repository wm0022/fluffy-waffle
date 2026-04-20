package com.shengwei.tushuguanli.config;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * 当前登录用户上下文工具类
 * 从 JwtAuthInterceptor 存入的请求属性中获取当前用户信息
 */
public class SecurityContext {

    private SecurityContext() {}

    /**
     * 获取当前登录用户 ID
     */
    public static Long getCurrentUserId() {
        Object userId = getRequest().getAttribute("currentUserId");
        if (userId instanceof Long) {
            return (Long) userId;
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Object username = getRequest().getAttribute("currentUsername");
        return username != null ? username.toString() : null;
    }

    /**
     * 获取当前用户的权限标识集合（由 PermissionInterceptor 查询并缓存到请求属性）
     */
    @SuppressWarnings("unchecked")
    public static Set<String> getCurrentUserPermissions() {
        Object perms = getRequest().getAttribute("userPermissions");
        if (perms instanceof Set) {
            return (Set<String>) perms;
        }
        return null;
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new IllegalStateException("不在 Web 请求上下文中，无法获取当前用户");
        }
        return attributes.getRequest();
    }
}
