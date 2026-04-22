package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.Customer;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.CustomerMapper;
import com.shengwei.tushuguanli.service.CustomerService;
import com.shengwei.tushuguanli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 顾客服务实现
 */
@Service
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String login(String username, String password) {
        Customer customer = getByUsername(username);
        if (customer == null) {
            throw new BusinessException("用户名不存在");
        }

        // 先用BCrypt比对，失败则尝试明文比对（兼容数据库中存明文密码的情况）
        boolean passwordMatch = passwordEncoder.matches(password, customer.getPassword());
        if (!passwordMatch && password.equals(customer.getPassword())) {
            // 明文匹配成功，自动升级为BCrypt加密存储
            Customer updateCustomer = new Customer();
            updateCustomer.setId(customer.getId());
            updateCustomer.setPassword(passwordEncoder.encode(password));
            customerMapper.updateById(updateCustomer);
            passwordMatch = true;
        }
        if (!passwordMatch) {
            throw new BusinessException("密码错误");
        }

        if (customer.getStatus() != 1) {
            throw new BusinessException("账户已被禁用");
        }

        return jwtUtil.generateToken(customer.getId(), customer.getUsername());
    }

    @Override
    public Customer getByUsername(String username) {
        LambdaQueryWrapper<Customer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Customer::getUsername, username);
        return customerMapper.selectOne(wrapper);
    }

    @Override
    public void register(String username, String password, String email, String phone) {
        if (getByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        LambdaQueryWrapper<Customer> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(Customer::getEmail, email);
        if (customerMapper.selectOne(emailWrapper) != null) {
            throw new BusinessException("邮箱已被注册");
        }

        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setStatus(1);
        customer.setMemberLevel(0);
        customer.setPoints(0);
        customer.setTotalAmount(java.math.BigDecimal.ZERO);

        customerMapper.insert(customer);
    }

    @Override
    public void logout() {
    }
}
