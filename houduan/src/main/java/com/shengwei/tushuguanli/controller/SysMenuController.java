package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.config.SecurityContext;
import com.shengwei.tushuguanli.entity.SysMenu;
import com.shengwei.tushuguanli.entity.SysRoleMenu;
import com.shengwei.tushuguanli.entity.SysUserRole;
import com.shengwei.tushuguanli.mapper.SysMenuMapper;
import com.shengwei.tushuguanli.mapper.SysRoleMenuMapper;
import com.shengwei.tushuguanli.mapper.SysUserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/menu")
public class SysMenuController {

    @Autowired
    private SysMenuMapper menuMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
        return Result.success(menus);
    }

    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        List<SysMenu> allMenus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>().orderByAsc(SysMenu::getSortOrder));
        List<SysMenu> tree = buildTree(allMenus, 0L);
        return Result.success(tree);
    }

    /**
     * 获取当前登录用户的菜单权限列表（基于 RBAC 模型）
     *
     * 数据流转链路:
     * userId -> sys_user_role(角色列表) -> sys_role_menu(菜单ID集合) -> sys_menu(菜单详情)
     * 只返回 menu_type=1(目录) 和 menu_type=2(菜单) 的条目，排除按钮级权限(menu_type=3)
     */
    @GetMapping("/my-menus")
    public Result<List<SysMenu>> getMyMenus() {
        Long userId = SecurityContext.getCurrentUserId();
        System.out.println("====== [my-menus] 开始查询菜单, userId=" + userId + " ======");

        // Step 1: 查询用户拥有的角色ID
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        System.out.println("[my-menus] Step1 - 用户角色IDs: " + roleIds);

        if (roleIds.isEmpty()) {
            System.out.println("[my-menus] 用户无任何角色，返回空菜单");
            return Result.success(new ArrayList<>());
        }

        // Step 2: 根据角色查询关联的菜单ID（去重）
        Set<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());

        System.out.println("[my-menus] Step2 - 角色关联的菜单IDs: " + menuIds);

        if (menuIds.isEmpty()) {
            System.out.println("[my-menus] 角色未关联任何菜单，返回空菜单");
            return Result.success(new ArrayList<>());
        }

        // Step 3: 查询这些菜单的完整信息（只查目录和菜单类型，排除按钮）
        List<SysMenu> menus = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getMenuId, menuIds)
                        .eq(SysMenu::getStatus, 1)           // 启用状态
                        .in(SysMenu::getMenuType, 1, 2)       // 目录 + 菜单，排除按钮(3)
                        .orderByAsc(SysMenu::getSortOrder)
        );

        System.out.println("[my-menus] Step3 - 查询到的菜单数量: " + menus.size());
        for (SysMenu m : menus) {
            System.out.println("  -> menuId=" + m.getMenuId() + ", name=" + m.getMenuName()
                    + ", parentId=" + m.getParentId() + ", type=" + m.getMenuType()
                    + ", path=" + m.getPath() + ", status=" + m.getStatus());
        }

        // Step 4: 构建树形结构
        List<SysMenu> tree = buildTree(menus, 0L);
        System.out.println("[my-menus] Step4 - 构建树形结构后数量: " + tree.size());

        // ========== 兜底策略：如果树形构建结果为空但原始数据不为空，尝试恢复 ==========
        if (tree.isEmpty() && !menus.isEmpty()) {
            System.out.println("[my-menus] !!! 警告: buildTree返回空但原始菜单数=" + menus.size()
                    + "，尝试兼容模式构建...");
            tree = fallbackBuildTree(menus);
            System.out.println("[my-menus] 兼容模式构建后数量: " + tree.size());
        }

        return Result.success(tree);
    }

    /**
     * 获取当前登录用户的所有权限标识（用于前端按钮级控制）
     * 返回该用户通过角色关联的所有菜单的 perms 字段集合
     * 前端可通过 v-if="hasPermission('book:delete')" 控制按钮显隐
     */
    @GetMapping("/my-perms")
    public Result<Set<String>> getMyPermissions() {
        Long userId = SecurityContext.getCurrentUserId();

        // 查询用户角色 -> 关联菜单 -> 提取 perms
        List<Long> roleIds = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
        ).stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return Result.success(Collections.emptySet());
        }

        Set<Long> menuIds = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .in(SysRoleMenu::getRoleId, roleIds)
        ).stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());

        // 提取所有非空权限标识（包含按钮级别的 perms）
        Set<String> perms = menuMapper.selectList(
                new LambdaQueryWrapper<SysMenu>()
                        .in(SysMenu::getMenuId, menuIds)
                        .eq(SysMenu::getStatus, 1)           // 启用状态
                        .isNotNull(SysMenu::getPerms)         // perms 非空
                        .ne(SysMenu::getPerms, "")
        ).stream()
                .map(SysMenu::getPerms)
                .filter(p -> p != null && !p.trim().isEmpty())
                .collect(Collectors.toSet());

        return Result.success(perms);
    }

    private List<SysMenu> buildTree(List<SysMenu> allMenus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                List<SysMenu> children = buildTree(allMenus, menu.getMenuId());
                if (!children.isEmpty()) {
                    menu.setChildren(children);
                }
                tree.add(menu);
            }
        }
        return tree;
    }

    /**
     * 兜底树形构建方法
     * 当标准 buildTree(menus, 0L) 返回空结果时调用
     * 尝试多种策略恢复菜单数据：
     * 1. parentId 为 NULL 或不存在的情况 → 将所有无父节点的菜单作为顶级节点
     * 2. 所有菜单的 parentId 都指向不存在的节点 → 找出所有不被其他菜单引用为父节点的菜单作为根
     * 3. 最终兜底 → 直接将所有菜单平铺返回（确保前端至少能看到菜单）
     */
    private List<SysMenu> fallbackBuildTree(List<SysMenu> allMenus) {
        // 策略1: 检查是否有 parentId == null 的记录（MySQL中可能存储为null而非0）
        boolean hasNullParent = allMenus.stream().anyMatch(m -> m.getParentId() == null);
        // 收集所有存在的 menuId
        Set<Long> existingIds = allMenus.stream().map(SysMenu::getMenuId).collect(Collectors.toSet());
        // 收集所有被引用的 parentId
        Set<Long> referencedParentIds = allMenus.stream()
                .map(SysMenu::getParentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<SysMenu> roots;

        if (hasNullParent) {
            // 策略1: parentId 为 null 的视为根节点
            System.out.println("[fallback] 使用策略1: 检测到parentId=null，将其作为根节点");
            roots = allMenus.stream()
                    .filter(m -> m.getParentId() == null)
                    .collect(Collectors.toList());
        } else {
            // 策略2: 找出不被任何其他菜单引用为父节点的菜单（真正的顶级菜单）
            referencedParentIds.remove(null);
            referencedParentIds.remove(0L);  // 排除0，因为用0没找到才走到这
            roots = allMenus.stream()
                    .filter(m -> !referencedParentIds.contains(m.getMenuId()))
                    .collect(Collectors.toList());
            System.out.println("[fallback] 使用策略2: 找到 " + roots.size() + " 个根节点(未被其他菜单引用)");
        }

        // 用这些根节点构建树
        List<SysMenu> result = new ArrayList<>();
        Set<Long> processed = new HashSet<>();

        for (SysMenu root : roots) {
            SysMenu copy = deepCopyMenu(root);
            findAndAttachChildren(copy, allMenus, processed);
            result.add(copy);
        }

        // 策略3: 如果策略1和2都未能构建出有效结构，直接返回平铺列表
        if (result.isEmpty() && !allMenus.isEmpty()) {
            System.out.println("[fallback] !!! 使用策略3: 平铺模式，返回全部 " + allMenus.size() + " 个菜单");
            result = allMenus;
        }

        return result;
    }

    /**
     * 递归查找并挂载子菜单
     */
    private void findAndAttachChildren(SysMenu parent, List<SysMenu> allMenus, Set<Long> processed) {
        if (processed.contains(parent.getMenuId())) {
            return;
        }
        processed.add(parent.getMenuId());

        List<SysMenu> children = allMenus.stream()
                .filter(m -> parent.getMenuId().equals(m.getParentId()))
                .collect(Collectors.toList());

        if (!children.isEmpty()) {
            List<SysMenu> childList = new ArrayList<>();
            for (SysMenu child : children) {
                SysMenu copy = deepCopyMenu(child);
                findAndAttachChildren(copy, allMenus, processed);
                childList.add(copy);
            }
            parent.setChildren(childList);
        }
    }

    /**
     * 深拷贝菜单对象（避免修改原始数据）
     */
    private SysMenu deepCopyMenu(SysMenu source) {
        SysMenu copy = new SysMenu();
        copy.setMenuId(source.getMenuId());
        copy.setParentId(source.getParentId());
        copy.setMenuName(source.getMenuName());
        copy.setMenuType(source.getMenuType());
        copy.setPath(source.getPath());
        copy.setComponent(source.getComponent());
        copy.setPerms(source.getPerms());
        copy.setIcon(source.getIcon());
        copy.setSortOrder(source.getSortOrder());
        copy.setStatus(source.getStatus());
        return copy;
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody SysMenu menu) {
        menuMapper.insert(menu);
        return Result.success("保存成功");
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysMenu menu) {
        menuMapper.updateById(menu);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        menuMapper.deleteById(id);
        return Result.success("删除成功");
    }
}
