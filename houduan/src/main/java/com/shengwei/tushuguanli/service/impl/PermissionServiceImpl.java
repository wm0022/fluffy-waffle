package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shengwei.tushuguanli.entity.SysMenu;
import com.shengwei.tushuguanli.entity.SysRoleMenu;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.entity.SysUserRole;
import com.shengwei.tushuguanli.mapper.SysMenuMapper;
import com.shengwei.tushuguanli.mapper.SysRoleMenuMapper;
import com.shengwei.tushuguanli.mapper.SysUserRoleMapper;
import com.shengwei.tushuguanli.service.PermissionService;
import com.shengwei.tushuguanli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private UserService userService;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @Autowired
    private SysMenuMapper menuMapper;

    @Override
    public List<String> getAdminMenuPaths(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        SysUser user = userService.getById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            return Collections.emptyList();
        }

        if (user.getUserType() == null || user.getUserType() != 1) {
            return Collections.emptyList();
        }

        List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));

        if (userRoles == null || userRoles.isEmpty()) {
            return getAllEnabledAdminMenuPaths();
        }

        Set<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return getAllEnabledAdminMenuPaths();
        }

        List<SysRoleMenu> roleMenus = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds));

        if (roleMenus == null || roleMenus.isEmpty()) {
            return getAllEnabledAdminMenuPaths();
        }

        Set<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            return getAllEnabledAdminMenuPaths();
        }

        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().in(SysMenu::getMenuId, menuIds).eq(SysMenu::getStatus, 1));

        return menus.stream()
                .map(SysMenu::getPath)
                .filter(path -> path != null && !path.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getAllEnabledAdminMenuPaths() {
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getStatus, 1));
        return menus.stream()
                .map(SysMenu::getPath)
                .filter(path -> path != null && !path.trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}

