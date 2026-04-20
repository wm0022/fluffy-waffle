package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysMenu;
import com.shengwei.tushuguanli.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/menu")
public class SysMenuController {

    @Autowired
    private SysMenuMapper menuMapper;

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

    private List<SysMenu> buildTree(List<SysMenu> allMenus, Long parentId) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                List<SysMenu> children = buildTree(allMenus, menu.getMenuId());
                menu.setChildren(children);
                tree.add(menu);
            }
        }
        return tree;
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
