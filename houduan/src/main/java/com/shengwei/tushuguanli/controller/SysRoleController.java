package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysRole;
import com.shengwei.tushuguanli.entity.SysRoleMenu;
import com.shengwei.tushuguanli.mapper.SysRoleMapper;
import com.shengwei.tushuguanli.mapper.SysRoleMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/role")
public class SysRoleController {

    @Autowired
    private SysRoleMapper roleMapper;

    @Autowired
    private SysRoleMenuMapper roleMenuMapper;

    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        List<SysRole> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }

    @GetMapping("/page")
    public Result<List<SysRole>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize) {
        List<SysRole> roles = roleMapper.selectList(null);
        return Result.success(roles);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestBody SysRole role) {
        roleMapper.insert(role);
        return Result.success("保存成功");
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestBody SysRole role) {
        roleMapper.updateById(role);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{roleId}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        List<SysRoleMenu> list = roleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));
        List<Long> menuIds = list.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toList());
        return Result.success(menuIds);
    }

    @PostMapping("/{roleId}/menus")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody java.util.Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> menuIdList = (List<Integer>) params.get("menuIds");

        // 删除原有权限
        roleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>().eq(SysRoleMenu::getRoleId, roleId));

        // 添加新权限
        if (menuIdList != null) {
            for (Integer menuId : menuIdList) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId.longValue());
                roleMenuMapper.insert(rm);
            }
        }
        return Result.success("权限分配成功");
    }
}
