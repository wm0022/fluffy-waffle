package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.entity.SysUserRole;
import com.shengwei.tushuguanli.mapper.SysUserRoleMapper;
import com.shengwei.tushuguanli.service.MemberService;
import com.shengwei.tushuguanli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

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
            int memberLevel = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
            user.setMemberLevel(memberLevel);
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
        if (!canAccessUser(id)) {
            return Result.forbidden("无权限访问");
        }
        SysUser user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        if (user.getId() == null || !canAccessUser(user.getId())) {
            return Result.forbidden("无权限操作");
        }
        SysUser existing = userService.getById(user.getId());
        if (existing != null) {
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            existing.setRealName(user.getRealName());
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
        }
        return Result.success("更新成功");
    }

    @GetMapping("/{id}/roles")
    public Result<List<Long>> getUserRoles(@PathVariable Long id) {
        if (!hasAdminMemberPermission()) {
            return Result.forbidden("无权限访问");
        }
        List<SysUserRole> roles = userRoleMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, id));
        List<Long> roleIds = roles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return Result.success(roleIds);
    }

    @PostMapping("/{id}/roles")
    public Result<Void> setUserRoles(@PathVariable Long id, @RequestBody java.util.Map<String, Object> params) {
        if (!hasAdminMemberPermission()) {
            return Result.forbidden("无权限操作");
        }
        @SuppressWarnings("unchecked")
        List<Integer> roleIds = (List<Integer>) params.get("roleIds");
        userRoleMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, id));
        if (roleIds != null) {
            for (Integer roleId : roleIds) {
                SysUserRole ur = new SysUserRole();
                ur.setUserId(id);
                ur.setRoleId(roleId.longValue());
                userRoleMapper.insert(ur);
            }
        }
        return Result.success("保存成功");
    }

    private boolean canAccessUser(Long targetUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return false;
        }
        return currentUserId.equals(targetUserId) || hasAdminMemberPermission();
    }

    private boolean hasAdminMemberPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("/admin/member".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long) {
            return (Long) authentication.getCredentials();
        }
        return null;
    }


    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
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
            user.setUserType(2);
        }
        if (user.getMemberLevel() == null) {
            user.setMemberLevel(0);
        }
        if (user.getPoints() == null) {
            user.setPoints(0);
        }
        if (user.getTotalAmount() == null) {
            user.setTotalAmount(java.math.BigDecimal.ZERO);
        }
        userService.save(user);
        return Result.success("创建成功");
    }
}
