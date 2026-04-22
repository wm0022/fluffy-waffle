package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.SysUser;

/**
 * 管理员用户服务
 * 注意：顾客相关操作请使用 CustomerService
 */
public interface UserService extends IService<SysUser> {

    /**
     * 管理员登录
     */
    String login(String username, String password);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 用户登出
     */
    void logout();
}
