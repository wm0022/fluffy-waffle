package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.DonationAccept;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.DonationAcceptMapper;
import com.shengwei.tushuguanli.service.DonationAcceptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 捐赠验收服务实现类
 */
@Service
public class DonationAcceptServiceImpl extends ServiceImpl<DonationAcceptMapper, DonationAccept> implements DonationAcceptService {

    @Override
    public Page<DonationAccept> pageAcceptList(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<DonationAccept> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DonationAccept> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            String donorName = (String) params.get("donorName");
            String donorPhone = (String) params.get("donorPhone");
            Integer acceptType = (Integer) params.get("acceptType");
            Integer acceptResult = (Integer) params.get("acceptResult");
            Integer status = (Integer) params.get("status");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");

            wrapper.like(StringUtils.hasText(donorName), DonationAccept::getDonorName, donorName)
                    .like(StringUtils.hasText(donorPhone), DonationAccept::getDonorPhone, donorPhone)
                    .eq(acceptType != null, DonationAccept::getAcceptType, acceptType)
                    .eq(acceptResult != null, DonationAccept::getAcceptResult, acceptResult)
                    .eq(status != null, DonationAccept::getStatus, status);

            // 时间范围查询
            if (StringUtils.hasText(startDate)) {
                wrapper.ge(DonationAccept::getCreateTime, startDate + " 00:00:00");
            }
            if (StringUtils.hasText(endDate)) {
                wrapper.le(DonationAccept::getCreateTime, endDate + " 23:59:59");
            }
        }

        wrapper.orderByDesc(DonationAccept::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createAccept(DonationAccept donationAccept) {
        // 验证必填字段
        if (StringUtils.isEmpty(donationAccept.getDonorName())) {
            throw new BusinessException("捐赠人姓名不能为空");
        }
        if (StringUtils.isEmpty(donationAccept.getDonorPhone())) {
            throw new BusinessException("捐赠人电话不能为空");
        }
        if (donationAccept.getAcceptType() == null) {
            throw new BusinessException("验收类型不能为空");
        }

        // 生成验收单号
        String acceptNo = "DA" + System.currentTimeMillis();
        donationAccept.setAcceptNo(acceptNo);

        // 设置默认状态
        if (donationAccept.getStatus() == null) {
            donationAccept.setStatus(1); // 待验收
        }

        save(donationAccept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAccept(DonationAccept donationAccept) {
        DonationAccept existAccept = getById(donationAccept.getAcceptId());
        if (existAccept == null) {
            throw new BusinessException("捐赠验收单不存在");
        }

        // 只有待验收状态的验收单可以修改
        if (existAccept.getStatus() != 1) {
            throw new BusinessException("只有待验收状态的验收单可以修改");
        }

        updateById(donationAccept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeAccept(Long acceptId) {
        DonationAccept accept = getById(acceptId);
        if (accept == null) {
            throw new BusinessException("捐赠验收单不存在");
        }

        // 只有已验收状态的验收单可以完成
        if (accept.getStatus() != 2) {
            throw new BusinessException("只有已验收状态的验收单可以完成");
        }

        accept.setStatus(3); // 已完成
        accept.setCompleteTime(LocalDateTime.now());
        updateById(accept);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void issueScore(Long acceptId) {
        DonationAccept accept = getById(acceptId);
        if (accept == null) {
            throw new BusinessException("捐赠验收单不存在");
        }

        // 只有已验收或已完成的验收单可以发放积分
        if (accept.getStatus() < 2) {
            throw new BusinessException("只有已验收或已完成的验收单可以发放积分");
        }

        // 积分已发放则不能重复发放
        if (accept.getScoreIssued() == 1) {
            throw new BusinessException("积分已发放，不能重复发放");
        }

        // TODO: 实际发放积分到捐赠人账户
        // 这里需要调用会员积分服务
        
        accept.setScoreIssued(1);
        updateById(accept);
    }

    @Override
    public Map<String, Object> countAcceptStats() {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = count();
        result.put("totalCount", totalCount);

        long pendingCount = count(new LambdaQueryWrapper<DonationAccept>()
                .eq(DonationAccept::getStatus, 1));
        result.put("pendingCount", pendingCount);

        long acceptedCount = count(new LambdaQueryWrapper<DonationAccept>()
                .eq(DonationAccept::getStatus, 2));
        result.put("acceptedCount", acceptedCount);

        long completedCount = count(new LambdaQueryWrapper<DonationAccept>()
                .eq(DonationAccept::getStatus, 3));
        result.put("completedCount", completedCount);

        // 验收结果统计
        long allAcceptedCount = count(new LambdaQueryWrapper<DonationAccept>()
                .eq(DonationAccept::getAcceptResult, 1));
        result.put("allAcceptedCount", allAcceptedCount);

        long partialAcceptedCount = count(new LambdaQueryWrapper<DonationAccept>()
                .eq(DonationAccept::getAcceptResult, 2));
        result.put("partialAcceptedCount", partialAcceptedCount);

        long allRejectedCount = count(new LambdaQueryWrapper<DonationAccept>()
                .eq(DonationAccept::getAcceptResult, 3));
        result.put("allRejectedCount", allRejectedCount);

        // 积分发放统计
        long scoreIssuedCount = count(new LambdaQueryWrapper<DonationAccept>()
                .eq(DonationAccept::getScoreIssued, 1));
        result.put("scoreIssuedCount", scoreIssuedCount);

        return result;
    }
}