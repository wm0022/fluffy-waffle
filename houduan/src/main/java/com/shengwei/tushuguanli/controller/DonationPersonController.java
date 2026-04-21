package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.PageResult;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.DonorInfo;
import com.shengwei.tushuguanli.service.DonorInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爱心赠书人士控制器（统一使用 donor_info 表）
 */
@RestController
@RequestMapping("/donation/person")
public class DonationPersonController {

    @Autowired
    private DonorInfoService donorInfoService;

    /**
     * 获取捐赠人士列表（管理员）
     * 兼容原 /donation/person/list 接口，内部委托给 DonorInfoService
     */
    @GetMapping("/list")
    public Result<List<DonorInfo>> getDonationPersonList(@RequestParam(required = false) Integer status) {
        // 使用 DonorInfo 的分页查询，返回全部记录（兼容原有接口）
        HashMap<String, Object> params = new HashMap<>();
        if (status != null) {
            params.put("status", status);
        }
        Page<DonorInfo> page = donorInfoService.pageDonorList(1, 1000, params);
        return Result.success(page.getRecords());
    }

    /**
     * 添加或更新捐赠人士（委托给 /donor 接口的 save/update）
     */
    @PostMapping("/save")
    public Result<Void> saveOrUpdatePerson(@RequestBody DonorInfo person) {
        if (person.getDonorId() != null) {
            donorInfoService.updateDonor(person);
        } else {
            donorInfoService.addDonor(person);
        }
        return Result.success("保存成功");
    }

    /**
     * 删除捐赠人士（委托给 /donor/{id}）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePerson(@PathVariable Long id) {
        donorInfoService.deleteDonor(id);
        return Result.success("删除成功");
    }

    /**
     * 审核捐赠人士（更新状态：1启用/0停用）
     */
    @PostMapping("/review")
    public Result<Void> reviewPerson(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        DonorInfo person = donorInfoService.getById(id);
        if (person == null) {
            return Result.error("捐赠人士不存在");
        }
        person.setStatus(status);
        donorInfoService.updateById(person);
        return Result.success("操作成功");
    }

    /**
     * 统计捐赠人士信息
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countPersons() {
        Map<String, Object> result = donorInfoService.countDonorStats();
        // 兼容原字段名映射
        Map<String, Object> compatResult = new HashMap<>();
        compatResult.put("total", result.get("totalCount"));
        compatResult.put("pending", result.get("inactiveCount"));
        compatResult.put("approved", result.get("activeCount"));
        compatResult.put("rejected", 0L);
        return Result.success(compatResult);
    }
}
