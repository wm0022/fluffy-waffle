package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.ServiceFeedback;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.ServiceFeedbackMapper;
import com.shengwei.tushuguanli.service.ServiceFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务反馈服务实现类
 */
@Service
public class ServiceFeedbackServiceImpl extends ServiceImpl<ServiceFeedbackMapper, ServiceFeedback> implements ServiceFeedbackService {

    @Override
    public Page<ServiceFeedback> pageFeedbackList(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<ServiceFeedback> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ServiceFeedback> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            String customerName = (String) params.get("customerName");
            String customerPhone = (String) params.get("customerPhone");
            Integer feedbackType = (Integer) params.get("feedbackType");
            String feedbackModule = (String) params.get("feedbackModule");
            Integer auditStatus = (Integer) params.get("auditStatus");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");

            wrapper.like(StringUtils.hasText(customerName), ServiceFeedback::getCustomerName, customerName)
                    .like(StringUtils.hasText(customerPhone), ServiceFeedback::getCustomerPhone, customerPhone)
                    .eq(feedbackType != null, ServiceFeedback::getFeedbackType, feedbackType)
                    .eq(StringUtils.hasText(feedbackModule), ServiceFeedback::getFeedbackModule, feedbackModule)
                    .eq(auditStatus != null, ServiceFeedback::getAuditStatus, auditStatus);

            // 时间范围查询
            if (StringUtils.hasText(startDate)) {
                wrapper.ge(ServiceFeedback::getCreateTime, startDate + " 00:00:00");
            }
            if (StringUtils.hasText(endDate)) {
                wrapper.le(ServiceFeedback::getCreateTime, endDate + " 23:59:59");
            }
        }

        wrapper.orderByDesc(ServiceFeedback::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addFeedback(ServiceFeedback serviceFeedback) {
        // 验证必填字段
        if (StringUtils.isEmpty(serviceFeedback.getCustomerName())) {
            throw new BusinessException("反馈人姓名不能为空");
        }
        if (StringUtils.isEmpty(serviceFeedback.getContent())) {
            throw new BusinessException("反馈内容不能为空");
        }

        // 生成反馈编号
        String feedbackNo = "FB" + System.currentTimeMillis();
        serviceFeedback.setFeedbackNo(feedbackNo);

        // 设置默认状态
        if (serviceFeedback.getAuditStatus() == null) {
            serviceFeedback.setAuditStatus(1); // 待处理
        }
        if (serviceFeedback.getSatisfaction() == null) {
            serviceFeedback.setSatisfaction(0); // 未评价
        }

        save(serviceFeedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFeedback(ServiceFeedback serviceFeedback) {
        ServiceFeedback existFeedback = getById(serviceFeedback.getFeedbackId());
        if (existFeedback == null) {
            throw new BusinessException("服务反馈不存在");
        }

        // 只有待处理状态的反馈可以修改
        if (existFeedback.getAuditStatus() != 1) {
            throw new BusinessException("只有待处理状态的反馈可以修改");
        }

        updateById(serviceFeedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleFeedback(Long feedbackId, String handleResult, Long handlerId, String handlerName) {
        ServiceFeedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("服务反馈不存在");
        }

        // 只有待处理或处理中状态的反馈可以处理
        if (feedback.getAuditStatus() != 1 && feedback.getAuditStatus() != 2) {
            throw new BusinessException("当前反馈状态不能处理");
        }

        feedback.setAuditStatus(3); // 已处理
        feedback.setHandleResult(handleResult);
        feedback.setHandlerId(handlerId);
        feedback.setHandlerName(handlerName);
        feedback.setHandleTime(LocalDateTime.now());
        updateById(feedback);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void closeFeedback(Long feedbackId) {
        ServiceFeedback feedback = getById(feedbackId);
        if (feedback == null) {
            throw new BusinessException("服务反馈不存在");
        }

        // 只有已处理状态的反馈可以关闭
        if (feedback.getAuditStatus() != 3) {
            throw new BusinessException("只有已处理状态的反馈可以关闭");
        }

        feedback.setAuditStatus(4); // 已关闭
        updateById(feedback);
    }

    @Override
    public Map<String, Object> countFeedbackStats(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = count();
        result.put("totalCount", totalCount);

        // 处理状态统计
        long pendingCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getAuditStatus, 1));
        result.put("pendingCount", pendingCount);

        long processingCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getAuditStatus, 2));
        result.put("processingCount", processingCount);

        long handledCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getAuditStatus, 3));
        result.put("handledCount", handledCount);

        long closedCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getAuditStatus, 4));
        result.put("closedCount", closedCount);

        // 反馈类型统计
        long suggestionCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getFeedbackType, 1));
        result.put("suggestionCount", suggestionCount);

        long complaintCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getFeedbackType, 2));
        result.put("complaintCount", complaintCount);

        long praiseCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getFeedbackType, 3));
        result.put("praiseCount", praiseCount);

        long inquiryCount = count(new LambdaQueryWrapper<ServiceFeedback>()
                .eq(ServiceFeedback::getFeedbackType, 4));
        result.put("inquiryCount", inquiryCount);

        return result;
    }
}