package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.ServiceFeedback;

import java.util.Map;

/**
 * 服务反馈服务接口
 */
public interface ServiceFeedbackService extends IService<ServiceFeedback> {

    /**
     * 分页查询服务反馈列表
     */
    Page<ServiceFeedback> pageFeedbackList(Integer pageNum, Integer pageSize, Map<String, Object> params);

    /**
     * 添加服务反馈
     */
    void addFeedback(ServiceFeedback serviceFeedback);

    /**
     * 更新服务反馈
     */
    void updateFeedback(ServiceFeedback serviceFeedback);

    /**
     * 处理反馈
     */
    void handleFeedback(Long feedbackId, String handleResult, Long handlerId, String handlerName);

    /**
     * 关闭反馈
     */
    void closeFeedback(Long feedbackId);

    /**
     * 统计服务反馈信息
     */
    Map<String, Object> countFeedbackStats(Map<String, Object> params);
}