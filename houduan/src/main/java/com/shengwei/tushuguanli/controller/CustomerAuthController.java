package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.Customer;
import com.shengwei.tushuguanli.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 顾客认证控制器（顾客端登录/注册/信息）
 */
@RestController
@RequestMapping("/customer/auth")
public class CustomerAuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /**
     * 顾客登录
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(
            @RequestParam String username,
            @RequestParam String password) {

        String token = customerService.login(username, password);
        Customer customer = customerService.getByUsername(username);
        // 安全起见，不返回密码字段
        if (customer != null) {
            customer.setPassword(null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", customer);

        return Result.success("登录成功", result);
    }

    /**
     * 顾客注册
     */
    @PostMapping("/register")
    public Result<Void> register(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String email,
            @RequestParam String phone) {

        customerService.register(username, password, email, phone);
        return Result.success("注册成功");
    }

    /**
     * 顾客登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        customerService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录顾客信息（从 JWT Token 中解析）
     */
    @GetMapping("/info")
    public Result<Customer> getCurrentCustomer() {
        Long customerId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (customerId == null) {
            return Result.error(401, "未登录");
        }
        Customer customer = customerService.getById(customerId);
        if (customer != null) {
            customer.setPassword(null);
        }
        return Result.success(customer);
    }

    /**
     * 修改密码（从 Token 获取当前用户，不允许传 userId）
     */
    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, Object> params) {
        Long currentCustomerId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentCustomerId == null) {
            return Result.error(401, "未登录");
        }

        String oldPassword = (String) params.get("oldPassword");
        String newPassword = (String) params.get("newPassword");

        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            return Result.error("密码格式不正确");
        }

        Customer customer = customerService.getById(currentCustomerId);
        if (customer == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            return Result.error("旧密码不正确");
        }
        customer.setPassword(passwordEncoder.encode(newPassword));
        customerService.updateById(customer);
        return Result.success("密码修改成功");
    }
}
