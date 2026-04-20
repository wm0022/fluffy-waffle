package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.SysUserMapper;
import com.shengwei.tushuguanli.service.UserService;
import com.shengwei.tushuguanli.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Service
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public String login(String username, String password) {
        SysUser user = getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }

        // 使用 BCrypt 比对密码
        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        
        if (!passwordMatch) {
            throw new BusinessException("密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用");
        }

        return jwtUtil.generateToken(user.getId(), user.getUsername());
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public void register(String username, String password, String email, String phone) {
        if (getByUsername(username) != null) {
            throw new BusinessException("用户名已存在");
        }

        LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUser::getEmail, email);
        if (userMapper.selectOne(emailWrapper) != null) {
            throw new BusinessException("邮箱已被注册");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setPhone(phone);
        user.setStatus(1);
        user.setUserType(2);

        userMapper.insert(user);
    }

    @Override
    public void logout() {
    }
}
