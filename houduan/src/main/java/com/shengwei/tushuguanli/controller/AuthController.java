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
 * 用户认证控制器
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
     * 用户登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password) {
        
        String token = userService.login(username, password);
        SysUser user = userService.getByUsername(username);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", user);

        return Result.success("登录成功", result);
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String phone) {
        
        userService.register(username, password, email, phone);
        return Result.success("注册成功");
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<SysUser> getCurrentUser() {
        return Result.error("未实现");
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String oldPassword = (String) params.get("oldPassword");
        String newPassword = (String) params.get("newPassword");
        
        SysUser user = userService.getById(userId);
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
