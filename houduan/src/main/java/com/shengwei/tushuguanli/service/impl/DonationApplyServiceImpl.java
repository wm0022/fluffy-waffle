package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.DonationApply;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.DonationApplyMapper;
import com.shengwei.tushuguanli.service.DonationApplyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 捐赠申请服务实现类
 */
@Service
public class DonationApplyServiceImpl extends ServiceImpl<DonationApplyMapper, DonationApply> implements DonationApplyService {

    @Override
    public Page<DonationApply> pageApplyList(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<DonationApply> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<DonationApply> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            String donorName = (String) params.get("donorName");
            String donorPhone = (String) params.get("donorPhone");
            Integer donationType = (Integer) params.get("donationType");
            Integer auditStatus = (Integer) params.get("auditStatus");
            Integer status = (Integer) params.get("status");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");

            wrapper.like(StringUtils.hasText(donorName), DonationApply::getDonorName, donorName)
                    .like(StringUtils.hasText(donorPhone), DonationApply::getDonorPhone, donorPhone)
                    .eq(donationType != null, DonationApply::getDonationType, donationType)
                    .eq(auditStatus != null, DonationApply::getAuditStatus, auditStatus)
                    .eq(status != null, DonationApply::getStatus, status);

            // 时间范围查询
            if (StringUtils.hasText(startDate)) {
                wrapper.ge(DonationApply::getCreateTime, startDate + " 00:00:00");
            }
            if (StringUtils.hasText(endDate)) {
                wrapper.le(DonationApply::getCreateTime, endDate + " 23:59:59");
            }
        }

        wrapper.orderByDesc(DonationApply::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createApply(DonationApply donationApply) {
        // 生成申请单号
        String applyNo = "DA" + System.currentTimeMillis();
        donationApply.setApplyNo(applyNo);
        
        // 设置默认状态
        donationApply.setAuditStatus(1); // 待审核
        donationApply.setStatus(1); // 待处理
        
        save(donationApply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateApply(DonationApply donationApply) {
        DonationApply existApply = getById(donationApply.getApplyId());
        if (existApply == null) {
            throw new BusinessException("捐赠申请不存在");
        }

        // 只有待审核状态的申请可以修改
        if (existApply.getAuditStatus() != 1) {
            throw new BusinessException("只有待审核状态的申请可以修改");
        }

        updateById(donationApply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditApply(Long applyId, Integer auditStatus, String auditRemark, Long auditUserId) {
        DonationApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("捐赠申请不存在");
        }

        // 只有待审核状态的申请可以审核
        if (apply.getAuditStatus() != 1) {
            throw new BusinessException("该申请已审核，不能重复审核");
        }

        apply.setAuditStatus(auditStatus);
        apply.setAuditRemark(auditRemark);
        apply.setAuditUserId(auditUserId);
        apply.setAuditTime(LocalDateTime.now());

        updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeApply(Long applyId) {
        DonationApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("捐赠申请不存在");
        }

        // 只有审核通过且未完成的申请可以完成
        if (apply.getAuditStatus() != 2) {
            throw new BusinessException("只有审核通过的申请可以完成");
        }
        if (apply.getStatus() == 2) {
            throw new BusinessException("该申请已完成，不能重复完成");
        }

        apply.setStatus(2);
        updateById(apply);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelApply(Long applyId) {
        DonationApply apply = getById(applyId);
        if (apply == null) {
            throw new BusinessException("捐赠申请不存在");
        }

        // 只有待审核状态的申请可以取消
        if (apply.getStatus() != 1) {
            throw new BusinessException("只有待处理的申请可以取消");
        }

        apply.setStatus(0);
        updateById(apply);
    }

    @Override
    public Map<String, Object> countApplyStats() {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = count();
        result.put("totalCount", totalCount);

        long pendingCount = count(new LambdaQueryWrapper<DonationApply>()
                .eq(DonationApply::getAuditStatus, 1));
        result.put("pendingCount", pendingCount);

        long approvedCount = count(new LambdaQueryWrapper<DonationApply>()
                .eq(DonationApply::getAuditStatus, 2));
        result.put("approvedCount", approvedCount);

        long rejectedCount = count(new LambdaQueryWrapper<DonationApply>()
                .eq(DonationApply::getAuditStatus, 3));
        result.put("rejectedCount", rejectedCount);

        long completedCount = count(new LambdaQueryWrapper<DonationApply>()
                .eq(DonationApply::getStatus, 2));
        result.put("completedCount", completedCount);

        // 捐赠类型统计
        long toStoreCount = count(new LambdaQueryWrapper<DonationApply>()
                .eq(DonationApply::getDonationType, 1));
        result.put("toStoreCount", toStoreCount);

        long homePickupCount = count(new LambdaQueryWrapper<DonationApply>()
                .eq(DonationApply::getDonationType, 2));
        result.put("homePickupCount", homePickupCount);

        return result;
    }
}
