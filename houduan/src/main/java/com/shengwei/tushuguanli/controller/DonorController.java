package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.PageResult;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.DonorInfo;
import com.shengwei.tushuguanli.service.DonorInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 捐赠人士管理控制器
 */
@RestController
@RequestMapping("/donor")
public class DonorController {

    @Autowired
    private DonorInfoService donorInfoService;

    /**
     * 分页查询捐赠人士列表
     */
    @GetMapping("/page")
    public Result<PageResult<DonorInfo>> pageDonorList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String realName,
            String phone,
            String idCard,
            Integer gender,
            Integer status) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("realName", realName);
        params.put("phone", phone);
        params.put("idCard", idCard);
        params.put("gender", gender);
        params.put("status", status);

        Page<DonorInfo> page = donorInfoService.pageDonorList(pageNum, pageSize, params);
        PageResult<DonorInfo> result = PageResult.of(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );

        return Result.success(result);
    }

    /**
     * 根据 ID 查询捐赠人士详情
     */
    @GetMapping("/{donorId}")
    public Result<DonorInfo> getDonorById(@PathVariable Long donorId) {
        DonorInfo donorInfo = donorInfoService.getById(donorId);
        if (donorInfo == null) {
            return Result.error("捐赠人士不存在");
        }
        return Result.success(donorInfo);
    }

    /**
     * 添加捐赠人士
     */
    @PostMapping
    public Result<Void> addDonor(@RequestBody DonorInfo donorInfo) {
        donorInfoService.addDonor(donorInfo);
        return Result.success("捐赠人士添加成功");
    }

    /**
     * 更新捐赠人士
     */
    @PutMapping
    public Result<Void> updateDonor(@RequestBody DonorInfo donorInfo) {
        donorInfoService.updateDonor(donorInfo);
        return Result.success("捐赠人士更新成功");
    }

    /**
     * 删除捐赠人士
     */
    @DeleteMapping("/{donorId}")
    public Result<Void> deleteDonor(@PathVariable Long donorId) {
        donorInfoService.deleteDonor(donorId);
        return Result.success("捐赠人士删除成功");
    }

    /**
     * 捐赠人士统计
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countDonorStats() {
        Map<String, Object> result = donorInfoService.countDonorStats();
        return Result.success(result);
    }

    /**
     * 根据身份证号查询捐赠人士
     */
    @GetMapping("/idCard/{idCard}")
    public Result<DonorInfo> getDonorByIdCard(@PathVariable String idCard) {
        DonorInfo donorInfo = donorInfoService.getByIdCard(idCard);
        if (donorInfo == null) {
            return Result.error("捐赠人士不存在");
        }
        return Result.success(donorInfo);
    }

    /**
     * 根据手机号查询捐赠人士
     */
    @GetMapping("/phone/{phone}")
    public Result<DonorInfo> getDonorByPhone(@PathVariable String phone) {
        DonorInfo donorInfo = donorInfoService.getByPhone(phone);
        if (donorInfo == null) {
            return Result.error("捐赠人士不存在");
        }
        return Result.success(donorInfo);
    }
}
