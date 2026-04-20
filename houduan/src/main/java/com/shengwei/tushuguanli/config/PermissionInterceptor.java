package com.shengwei.tushuguanli.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shengwei.tushuguanli.annotation.RequirePermission;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysMenu;
import com.shengwei.tushuguanli.mapper.SysMenuMapper;
import com.shengwei.tushuguanli.mapper.SysRoleMenuMapper;
import com.shengwei.tushuguanli.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 权限校验拦截器
 * 检查 @RequirePermission 注解，根据用户角色关联的菜单权限标识进行校验
 *
 * 执行顺序: SecurityConfig -> JwtAuthInterceptor(认证) -> PermissionInterceptor(授权) -> Controller
 */
@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 当前用户权限缓存（请求级别，避免同一请求内重复查询）
     */
    private static final String ATTR_USER_PERMS = "userPermissions";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 只处理方法级别的处理器（排除静态资源等）
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        // 获取方法或类上的 @RequirePermission 注解
        RequirePermission requirePermission = getRequirePermission(handlerMethod);

        // 无权限注解，直接放行
        if (requirePermission == null || requirePermission.value().length == 0) {
            return true;
        }

        Long userId = SecurityContext.getCurrentUserId();
        if (userId == null) {
            writeForbidden(response, "未登录");
            return false;
        }

        // 获取当前用户的权限集合
        Set<String> userPerms = getUserPermissions(request, userId);
        if (userPerms == null || userPerms.isEmpty()) {
            writeForbidden(response, "无访问权限，请联系管理员分配角色");
            return false;
        }

        // 检查是否拥有所需权限之一（OR 逻辑）
        String[] requiredPerms = requirePermission.value();
        for (String perm : requiredPerms) {
            if (userPerms.contains(perm.trim())) {
                return true;  // 有权限，放行
            }
        }

        writeForbidden(response, "权限不足，无法执行该操作");
        return false;
    }

    /**
     * 从方法或类上获取 @RequirePermission 注解
     */
    private RequirePermission getRequirePermission(HandlerMethod handlerMethod) {
        // 优先取方法上的注解
        RequirePermission methodAnnotation = handlerMethod.getMethodAnnotation(RequirePermission.class);
        if (methodAnnotation != null && methodAnnotation.value().length > 0) {
            return methodAnnotation;
        }
        // 其次取类上的注解
        return handlerMethod.getBeanType().getAnnotation(RequirePermission.class);
    }

    /**
     * 获取当前用户的所有权限标识集合
     * 数据流: userId -> sys_user_role(roleIds) -> sys_role_menu(menuIds) -> sys_menu(perms)
     *
     * 使用请求级缓存：同一请求中只查询一次数据库
     */
    @SuppressWarnings("unchecked")
    private Set<String> getUserPermissions(HttpServletRequest request, Long userId) {
        Object cached = request.getAttribute(ATTR_USER_PERMS);
        if (cached != null) {
            return (Set<String>) cached;
        }

        // Step 1: 查询用户拥有的角色ID列表
        List<Long> roleIds = userRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.shengwei.tushuguanli.entity.SysUserRole>()
                        .eq(com.shengwei.tushuguanli.entity.SysUserRole::getUserId, userId)
        ).stream().map(com.shengwei.tushuguanli.entity.SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return null;
        }

        // Step 2: 根据角色ID查询关联的菜单ID列表
        List<Long> menuIds = roleMenuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.shengwei.tushuguanli.entity.SysRoleMenu>()
                        .in(com.shengwei.tushuguanli.entity.SysRoleMenu::getRoleId, roleIds)
        ).stream().map(com.shengwei.tushuguanli.entity.SysRoleMenu::getMenuId).distinct().collect(Collectors.toList());

        if (menuIds.isEmpty()) {
            return null;
        }

        // Step 3: 根据菜单ID查询权限标识（只查非空的 perms）
        Set<String> perms = menuMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getMenuId, menuIds)
                        .isNotNull(SysMenu::getPerms)
                        .ne(SysMenu::getPerms, "")
        ).stream()
                .map(SysMenu::getPerms)
                .filter(p -> p != null && !p.trim().isEmpty())
                .collect(Collectors.toSet());

        request.setAttribute(ATTR_USER_PERMS, perms);
        return perms;
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        Result<?> result = Result.error(403, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
