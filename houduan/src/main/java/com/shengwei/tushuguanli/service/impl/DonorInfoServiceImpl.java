package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.DonorInfo;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.DonorInfoMapper;
import com.shengwei.tushuguanli.service.DonorInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 捐赠人士服务实现类
 */
@Service
public class DonorInfoServiceImpl extends ServiceImpl<DonorInfoMapper, DonorInfo> implements DonorInfoService {

    @Override
    public Page<DonorInfo> pageDonorList(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<DonorInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DonorInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            String realName = (String) params.get("realName");
            String phone = (String) params.get("phone");
            String idCard = (String) params.get("idCard");
            Integer gender = (Integer) params.get("gender");
            Integer status = (Integer) params.get("status");

            wrapper.like(StringUtils.hasText(realName), DonorInfo::getRealName, realName)
                    .like(StringUtils.hasText(phone), DonorInfo::getPhone, phone)
                    .eq(StringUtils.hasText(idCard), DonorInfo::getIdCard, idCard)
                    .eq(gender != null, DonorInfo::getGender, gender)
                    .eq(status != null, DonorInfo::getStatus, status);
        }

        wrapper.orderByDesc(DonorInfo::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    public DonorInfo getByIdCard(String idCard) {
        LambdaQueryWrapper<DonorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DonorInfo::getIdCard, idCard);
        return getOne(wrapper);
    }

    @Override
    public DonorInfo getByPhone(String phone) {
        LambdaQueryWrapper<DonorInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DonorInfo::getPhone, phone);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDonor(DonorInfo donorInfo) {
        // 检查身份证号是否已存在
        if (StringUtils.hasText(donorInfo.getIdCard())) {
            DonorInfo existByIdCard = getByIdCard(donorInfo.getIdCard());
            if (existByIdCard != null) {
                throw new BusinessException("该身份证号已存在");
            }
        }

        // 检查手机号是否已存在
        DonorInfo existByPhone = getByPhone(donorInfo.getPhone());
        if (existByPhone != null) {
            throw new BusinessException("该手机号已存在");
        }

        save(donorInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDonor(DonorInfo donorInfo) {
        DonorInfo existDonor = getById(donorInfo.getDonorId());
        if (existDonor == null) {
            throw new BusinessException("捐赠人士不存在");
        }

        // 检查身份证号是否被其他捐赠人士使用
        if (StringUtils.hasText(donorInfo.getIdCard()) && 
            !donorInfo.getIdCard().equals(existDonor.getIdCard())) {
            DonorInfo idCardExist = getByIdCard(donorInfo.getIdCard());
            if (idCardExist != null && !idCardExist.getDonorId().equals(donorInfo.getDonorId())) {
                throw new BusinessException("该身份证号已被其他捐赠人士使用");
            }
        }

        // 检查手机号是否被其他捐赠人士使用
        if (!donorInfo.getPhone().equals(existDonor.getPhone())) {
            DonorInfo phoneExist = getByPhone(donorInfo.getPhone());
            if (phoneExist != null && !phoneExist.getDonorId().equals(donorInfo.getDonorId())) {
                throw new BusinessException("该手机号已被其他捐赠人士使用");
            }
        }

        updateById(donorInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDonor(Long donorId) {
        DonorInfo donorInfo = getById(donorId);
        if (donorInfo == null) {
            throw new BusinessException("捐赠人士不存在");
        }

        removeById(donorId);
    }

    @Override
    public Map<String, Object> countDonorStats() {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = count();
        result.put("totalCount", totalCount);

        long activeCount = count(new LambdaQueryWrapper<DonorInfo>()
                .eq(DonorInfo::getStatus, 1));
        result.put("activeCount", activeCount);

        long inactiveCount = totalCount - activeCount;
        result.put("inactiveCount", inactiveCount);

        // 统计捐赠信息
        result.put("totalBooks", 0);
        result.put("totalAmount", 0);
        result.put("totalScore", 0);

        return result;
    }
}
