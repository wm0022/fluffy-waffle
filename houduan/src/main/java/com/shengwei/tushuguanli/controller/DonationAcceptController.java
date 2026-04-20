package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.PageResult;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.DonationAccept;
import com.shengwei.tushuguanli.service.DonationAcceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 捐赠验收管理控制器
 */
@RestController
@RequestMapping("/donation-accept")
public class DonationAcceptController {

    @Autowired
    private DonationAcceptService donationAcceptService;

    /**
     * 分页查询捐赠验收列表
     */
    @GetMapping("/page")
    public Result<PageResult<DonationAccept>> pageAcceptList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String donorName,
            String donorPhone,
            Integer acceptType,
            Integer acceptResult,
            Integer status,
            String startDate,
            String endDate) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("donorName", donorName);
        params.put("donorPhone", donorPhone);
        params.put("acceptType", acceptType);
        params.put("acceptResult", acceptResult);
        params.put("status", status);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Page<DonationAccept> page = donationAcceptService.pageAcceptList(pageNum, pageSize, params);
        PageResult<DonationAccept> result = PageResult.of(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );

        return Result.success(result);
    }

    /**
     * 根据 ID 查询捐赠验收详情
     */
    @GetMapping("/{acceptId}")
    public Result<DonationAccept> getAcceptById(@PathVariable Long acceptId) {
        DonationAccept accept = donationAcceptService.getById(acceptId);
        if (accept == null) {
            return Result.error("捐赠验收单不存在");
        }
        return Result.success(accept);
    }

    /**
     * 创建捐赠验收单
     */
    @PostMapping
    public Result<Void> createAccept(@RequestBody DonationAccept donationAccept) {
        donationAcceptService.createAccept(donationAccept);
        return Result.success("捐赠验收单创建成功");
    }

    /**
     * 更新捐赠验收单
     */
    @PutMapping
    public Result<Void> updateAccept(@RequestBody DonationAccept donationAccept) {
        donationAcceptService.updateAccept(donationAccept);
        return Result.success("捐赠验收单更新成功");
    }

    /**
     * 完成验收
     */
    @PostMapping("/{acceptId}/complete")
    public Result<Void> completeAccept(@PathVariable Long acceptId) {
        donationAcceptService.completeAccept(acceptId);
        return Result.success("捐赠验收已完成");
    }

    /**
     * 发放积分
     */
    @PostMapping("/{acceptId}/issue-score")
    public Result<Void> issueScore(@PathVariable Long acceptId) {
        donationAcceptService.issueScore(acceptId);
        return Result.success("积分发放成功");
    }

    /**
     * 捐赠验收统计
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countAcceptStats() {
        Map<String, Object> result = donationAcceptService.countAcceptStats();
        return Result.success(result);
    }
}