package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.PageResult;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.ServiceFeedback;
import com.shengwei.tushuguanli.service.ServiceFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 服务反馈管理控制器
 */
@RestController
@RequestMapping("/service-feedback")
public class ServiceFeedbackController {

    @Autowired
    private ServiceFeedbackService serviceFeedbackService;

    /**
     * 分页查询服务反馈列表
     */
    @GetMapping("/page")
    public Result<PageResult<ServiceFeedback>> pageFeedbackList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String customerName,
            String customerPhone,
            Integer feedbackType,
            String feedbackModule,
            Integer auditStatus,
            String startDate,
            String endDate) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("customerName", customerName);
        params.put("customerPhone", customerPhone);
        params.put("feedbackType", feedbackType);
        params.put("feedbackModule", feedbackModule);
        params.put("auditStatus", auditStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Page<ServiceFeedback> page = serviceFeedbackService.pageFeedbackList(pageNum, pageSize, params);
        PageResult<ServiceFeedback> result = PageResult.of(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );

        return Result.success(result);
    }

    /**
     * 根据 ID 查询反馈详情
     */
    @GetMapping("/{feedbackId}")
    public Result<ServiceFeedback> getFeedbackById(@PathVariable Long feedbackId) {
        ServiceFeedback feedback = serviceFeedbackService.getById(feedbackId);
        if (feedback == null) {
            return Result.error("服务反馈不存在");
        }
        return Result.success(feedback);
    }

    /**
     * 添加服务反馈
     */
    @PostMapping
    public Result<Void> addFeedback(@RequestBody ServiceFeedback serviceFeedback) {
        serviceFeedbackService.addFeedback(serviceFeedback);
        return Result.success("服务反馈添加成功");
    }

    /**
     * 更新服务反馈
     */
    @PutMapping
    public Result<Void> updateFeedback(@RequestBody ServiceFeedback serviceFeedback) {
        serviceFeedbackService.updateFeedback(serviceFeedback);
        return Result.success("服务反馈更新成功");
    }

    /**
     * 处理反馈
     */
    @PostMapping("/{feedbackId}/handle")
    public Result<Void> handleFeedback(
            @PathVariable Long feedbackId,
            @RequestParam String handleResult,
            @RequestParam Long handlerId,
            @RequestParam String handlerName) {
        serviceFeedbackService.handleFeedback(feedbackId, handleResult, handlerId, handlerName);
        return Result.success("反馈处理成功");
    }

    /**
     * 关闭反馈
     */
    @PostMapping("/{feedbackId}/close")
    public Result<Void> closeFeedback(@PathVariable Long feedbackId) {
        serviceFeedbackService.closeFeedback(feedbackId);
        return Result.success("反馈关闭成功");
    }

    /**
     * 服务反馈统计
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countFeedbackStats(
            String startDate,
            String endDate) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Map<String, Object> result = serviceFeedbackService.countFeedbackStats(params);
        return Result.success(result);
    }
}