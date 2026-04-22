package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.Customer;
import com.shengwei.tushuguanli.entity.DonorInfo;
import com.shengwei.tushuguanli.mapper.DonorInfoMapper;
import com.shengwei.tushuguanli.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 顾客管理控制器（管理员端）
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DonorInfoMapper donorInfoMapper;

    /**
     * 分页查询顾客列表（支持按用户名/姓名/手机号模糊搜索）
     */
    @GetMapping("/page")
    public Result<Page<Customer>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) String phone) {

        Page<Customer> page = new Page<>(pageNum, pageSize);
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
        if (username != null && !username.trim().isEmpty()) {
            queryWrapper.like("username", username.trim());
        }
        if (realName != null && !realName.trim().isEmpty()) {
            queryWrapper.like("real_name", realName.trim());
        }
        if (phone != null && !phone.trim().isEmpty()) {
            queryWrapper.like("phone", phone.trim());
        }
        queryWrapper.orderByDesc("create_time");
        Page<Customer> result = customerService.page(page, queryWrapper);

        for (Customer customer : result.getRecords()) {
            customer.setPassword(null);
        }

        return Result.success(result);
    }

    /**
     * 查询全部顾客列表
     */
    @GetMapping("/list")
    public Result<List<Customer>> list() {
        QueryWrapper<Customer> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("create_time");
        List<Customer> result = customerService.list(queryWrapper);

        for (Customer customer : result) {
            customer.setPassword(null);
        }

        return Result.success(result);
    }

    /**
     * 按ID查询顾客详情
     */
    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        Customer customer = customerService.getById(id);
        if (customer != null) {
            customer.setPassword(null);
        }
        return Result.success(customer);
    }

    /**
     * 更新顾客信息
     */
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        Customer existing = customerService.getById(customer.getId());
        if (existing != null) {
            existing.setEmail(customer.getEmail());
            existing.setPhone(customer.getPhone());
            existing.setRealName(customer.getRealName());
            existing.setGender(customer.getGender());
            existing.setIdCard(customer.getIdCard());
            existing.setNation(customer.getNation());
            existing.setNativePlace(customer.getNativePlace());
            existing.setBirthDate(customer.getBirthDate());
            existing.setAge(customer.getAge());
            existing.setEducation(customer.getEducation());
            existing.setAddress(customer.getAddress());
            existing.setPreferences(customer.getPreferences());
            customerService.updateById(existing);

            // 同步个人信息到 donor_info 表（赠书人士管理）
            syncToDonorInfo(customer);
        }
        return Result.success("更新成功");
    }

    /**
     * 将顾客更新的个人信息同步到 donor_info 表
     */
    private void syncToDonorInfo(Customer customer) {
        try {
            LambdaQueryWrapper<DonorInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DonorInfo::getUserId, customer.getId());
            DonorInfo donor = donorInfoMapper.selectOne(wrapper);
            if (donor != null) {
                donor.setRealName(customer.getRealName());
                donor.setIdCard(customer.getIdCard());
                donor.setGender(customer.getGender());
                donor.setNation(customer.getNation());
                donor.setNativePlace(customer.getNativePlace());
                donor.setBirthDate(customer.getBirthDate());
                donor.setAge(customer.getAge());
                donor.setEducation(customer.getEducation());
                donor.setAddress(customer.getAddress());
                donor.setPhone(customer.getPhone());
                donor.setEmail(customer.getEmail());
                donorInfoMapper.updateById(donor);
            }
        } catch (Exception e) {
            // 同步失败不影响主流程，仅记录日志
            System.err.println("[CustomerController] 同步donor_info失败: " + e.getMessage());
        }
    }

    /**
     * 删除顾客
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        customerService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 创建顾客（管理员端）
     */
    @PostMapping("/create")
    public Result<Void> createCustomer(@RequestBody Customer customer) {
        if (customer.getUsername() == null || customer.getUsername().trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }
        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        if (customerService.getByUsername(customer.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        if (customer.getStatus() == null) {
            customer.setStatus(1);
        }
        if (customer.getMemberLevel() == null) {
            customer.setMemberLevel(0);
        }
        if (customer.getPoints() == null) {
            customer.setPoints(0);
        }
        if (customer.getTotalAmount() == null) {
            customer.setTotalAmount(java.math.BigDecimal.ZERO);
        }
        customerService.save(customer);
        return Result.success("创建成功");
    }
}
