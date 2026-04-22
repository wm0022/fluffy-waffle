package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.entity.SysUserRole;
import com.shengwei.tushuguanli.mapper.SysUserRoleMapper;
import com.shengwei.tushuguanli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @GetMapping("/page")
    public Result<Page<SysUser>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.ne("username", "admin");
        queryWrapper.orderByDesc("create_time");
        Page<SysUser> result = userService.page(page, queryWrapper);
        
        for (SysUser user : result.getRecords()) {
            user.setPassword(null);
        }
        
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<SysUser> result = userService.list(queryWrapper);
        
        for (SysUser user : result) {
            user.setPassword(null);
        }
        
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        SysUser existing = userService.getById(user.getId());
        if (existing != null) {
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            existing.setRealName(user.getRealName());
            existing.setUserType(user.getUserType());
            existing.setGender(user.getGender());
            existing.setIdCard(user.getIdCard());
            existing.setNation(user.getNation());
            existing.setNativePlace(user.getNativePlace());
            existing.setBirthDate(user.getBirthDate());
            existing.setAge(user.getAge());
            existing.setEducation(user.getEducation());
            existing.setAddress(user.getAddress());
            existing.setPreferences(user.getPreferences());
            userService.updateById(existing);

            // 同步更新用户角色关联表（RBAC 权限链路）
            if (user.getUserType() != null) {
                syncUserRole(existing.getId(), user.getUserType().longValue());
            }
        }
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        // 删除用户时同步清理角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        userService.removeById(id);
        return Result.success("删除成功");
    }

    @PostMapping("/create")
    public Result<Void> createUser(@RequestBody SysUser user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        SysUser existing = userService.getByUsername(user.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (user.getUserType() == null) {
            user.setUserType(1);
        }
        userService.save(user);

        // 创建用户后写入角色关联表（RBAC 权限链路）
        syncUserRole(user.getId(), user.getUserType().longValue());

        return Result.success("创建成功");
    }

    /**
     * 同步用户角色到 sys_user_role 关联表
     * 原理：先删除该用户的旧角色记录，再插入新的角色记录（保证唯一性）
     *
     * @param userId  用户ID
     * @param roleId  角色ID（即 sys_user.user_type 的值，对应 sys_role.role_id）
     */
    private void syncUserRole(Long userId, Long roleId) {
        // 删除该用户的所有旧角色关联
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        // 插入新的角色关联
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);
    }
}
