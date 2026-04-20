package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.DonationPerson;
import com.shengwei.tushuguanli.mapper.DonationPersonMapper;
import com.shengwei.tushuguanli.service.DonationPersonService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 捐赠人士服务实现
 */
@Service
public class DonationPersonServiceImpl extends ServiceImpl<DonationPersonMapper, DonationPerson> implements DonationPersonService {

    @Override
    public List<DonationPerson> getDonationPersonList(Integer status) {
        LambdaQueryWrapper<DonationPerson> wrapper = new LambdaQueryWrapper<>();
        if (status != null && status >= 0) {
            wrapper.eq(DonationPerson::getStatus, status);
        }
        wrapper.orderByDesc(DonationPerson::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void saveOrUpdatePerson(DonationPerson person) {
        if (person.getId() == null) {
            person.setCreateTime(LocalDateTime.now());
        }
        person.setUpdateTime(LocalDateTime.now());
        
        // 处理空字符串导致的日期字段问题
        if (person.getBirthDate() != null && person.getBirthDate().getYear() == 1970) {
            person.setBirthDate(null);
        }
        
        saveOrUpdate(person);
    }

    @Override
    public Map<String, Object> countPersons() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", count());
        result.put("pending", count(new LambdaQueryWrapper<DonationPerson>().eq(DonationPerson::getStatus, 0)));
        result.put("approved", count(new LambdaQueryWrapper<DonationPerson>().eq(DonationPerson::getStatus, 1)));
        result.put("rejected", count(new LambdaQueryWrapper<DonationPerson>().eq(DonationPerson::getStatus, 2)));
        return result;
    }
}
