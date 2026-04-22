package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.DonationRecord;
import com.shengwei.tushuguanli.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 捐赠控制器
 */
@RestController
@RequestMapping("/donation")
public class DonationController {

    @Autowired
    private DonationService donationService;

    /**
     * 提交捐赠申请
     */
    @PostMapping("/submit")
    public Result<Void> submitDonation(@RequestBody DonationRecord record) {
        donationService.submitDonation(record);
        return Result.success("提交成功");
    }

    /**
     * 获取当前用户捐赠记录
     */
    @GetMapping("/myList")
    public Result<List<DonationRecord>> getUserDonations() {
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        List<DonationRecord> list = donationService.getUserDonations(currentUserId);
        return Result.success(list);
    }

    /**
     * 获取所有捐赠记录（管理员）
     */
    @GetMapping("/all")
    public Result<List<DonationRecord>> getAllDonations(@RequestParam(required = false) Integer status) {
        List<DonationRecord> list = donationService.getAllDonations(status);
        return Result.success(list);
    }

    /**
     * 审核捐赠记录
     */
    @PostMapping("/review")
    public Result<Void> reviewDonation(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String reviewRemark = params.get("reviewRemark") != null ? params.get("reviewRemark").toString() : null;
        // 审核人ID从Token获取，不接受前端传入（防伪造审核记录）
        Long reviewerId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (reviewerId == null) {
            return Result.error(401, "请先登录");
        }

        donationService.reviewDonation(id, status, reviewRemark, reviewerId);
        return Result.success("审核成功");
    }

    /**
     * 更新捐赠记录
     */
    @PostMapping("/update")
    public Result<Void> updateDonation(@RequestBody DonationRecord record) {
        donationService.updateDonation(record);
        return Result.success("更新成功");
    }

    /**
     * 统计捐赠信息
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countDonations() {
        Map<String, Object> result = donationService.countDonations();
        return Result.success(result);
    }
}
