package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.Customer;

/**
 * 顾客服务接口
 */
public interface CustomerService extends IService<Customer> {

    /**
     * 顾客登录
     */
    String login(String username, String password);

    /**
     * 顾客注册
     */
    void register(String username, String password, String email, String phone);

    /**
     * 根据用户名查询顾客
     */
    Customer getByUsername(String username);

    /**
     * 顾客登出
     */
    void logout();
}
