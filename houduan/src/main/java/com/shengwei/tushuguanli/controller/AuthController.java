package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.service.UserService;
import com.shengwei.tushuguanli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理员认证控制器
 * 注意：顾客登录/注册/信息请使用 CustomerAuthController (/customer/auth/*)
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password) {
        
        String token = userService.login(username, password);
        SysUser user = userService.getByUsername(username);
        if (user != null) {
            user.setPassword(null);  // 安全：不返回密码哈希值
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);

        return Result.success("登录成功", result);
    }

    /**
     * 管理员登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录管理员信息（从 JWT Token 中解析）
     */
    @GetMapping("/info")
    public Result<SysUser> getCurrentUser() {
        Long userId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        SysUser user = userService.getById(userId);
        // 安全起见，不返回密码字段
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    /**
     * 修改密码（从 Token 获取当前用户，不允许传 userId）
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, Object> params) {
        // 强制使用当前登录用户 ID，不接受前端传入的 userId（防篡改）
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }

        String oldPassword = (String) params.get("oldPassword");
        String newPassword = (String) params.get("newPassword");

        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            return Result.error("密码格式不正确");
        }

        SysUser user = userService.getById(currentUserId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error("旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        return Result.success("密码修改成功");
    }
}
