package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.PageResult;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.DonationApply;
import com.shengwei.tushuguanli.service.DonationApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 捐赠申请管理控制器
 */
@RestController
@RequestMapping("/donation-apply")
public class DonationApplyController {

    @Autowired
    private DonationApplyService donationApplyService;

    /**
     * 分页查询捐赠申请列表
     */
    @GetMapping("/page")
    public Result<PageResult<DonationApply>> pageApplyList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String donorName,
            String donorPhone,
            Integer donationType,
            Integer auditStatus,
            Integer status,
            String startDate,
            String endDate) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("donorName", donorName);
        params.put("donorPhone", donorPhone);
        params.put("donationType", donationType);
        params.put("auditStatus", auditStatus);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Page<DonationApply> page = donationApplyService.pageApplyList(pageNum, pageSize, params);
        PageResult<DonationApply> result = PageResult.of(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );

        return Result.success(result);
    }

    /**
     * 根据 ID 查询捐赠申请详情
     */
    @GetMapping("/{applyId}")
    public Result<DonationApply> getApplyById(@PathVariable Long applyId) {
        DonationApply apply = donationApplyService.getById(applyId);
        if (apply == null) {
            return Result.error("捐赠申请不存在");
        }
        return Result.success(apply);
    }

    /**
     * 创建捐赠申请
     */
    @PostMapping
    public Result<Void> createApply(@RequestBody DonationApply donationApply) {
        donationApplyService.createApply(donationApply);
        return Result.success("捐赠申请创建成功");
    }

    /**
     * 更新捐赠申请
     */
    @PutMapping
    public Result<Void> updateApply(@RequestBody DonationApply donationApply) {
        donationApplyService.updateApply(donationApply);
        return Result.success("捐赠申请更新成功");
    }

    /**
     * 审核捐赠申请
     */
    @PostMapping("/{applyId}/audit")
    public Result<Void> auditApply(
            @PathVariable Long applyId,
            @RequestParam Integer auditStatus,
            @RequestParam String auditRemark,
            @RequestParam(required = false) Long auditUserId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.unauthorized("未登录或登录已过期");
        }
        auditUserId = currentUserId;
        donationApplyService.auditApply(applyId, auditStatus, auditRemark, auditUserId);
        return Result.success("审核操作成功");
    }

    /**
     * 完成捐赠申请
     */
    @PostMapping("/{applyId}/complete")
    public Result<Void> completeApply(@PathVariable Long applyId) {
        donationApplyService.completeApply(applyId);
        return Result.success("捐赠申请已完成");
    }

    /**
     * 取消捐赠申请
     */
    @PostMapping("/{applyId}/cancel")
    public Result<Void> cancelApply(@PathVariable Long applyId) {
        donationApplyService.cancelApply(applyId);
        return Result.success("捐赠申请已取消");
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long) {
            return (Long) authentication.getCredentials();
        }
        return null;
    }


    /**
     * 捐赠申请统计
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countApplyStats() {
        Map<String, Object> result = donationApplyService.countApplyStats();
        return Result.success(result);
    }
}
