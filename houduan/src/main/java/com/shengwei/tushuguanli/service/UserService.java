package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.SysUser;

/**
 * 用户服务
 */
public interface UserService extends IService<SysUser> {

    /**
     * 用户登录
     */
    String login(String username, String password);

    /**
     * 用户注册
     */
    void register(String username, String password, String email, String phone);

    /**
     * 根据用户名查询用户
     */
    SysUser getByUsername(String username);

    /**
     * 用户登出
     */
    void logout();
}
