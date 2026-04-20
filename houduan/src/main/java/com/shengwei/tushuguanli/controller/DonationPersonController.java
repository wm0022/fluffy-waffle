package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.DonationPerson;
import com.shengwei.tushuguanli.service.DonationPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 捐赠人士控制器
 */
@RestController
@RequestMapping("/donation/person")
public class DonationPersonController {

    @Autowired
    private DonationPersonService donationPersonService;

    /**
     * 获取捐赠人士列表（管理员）
     */
    @GetMapping("/list")
    public Result<List<DonationPerson>> getDonationPersonList(@RequestParam(required = false) Integer status) {
        List<DonationPerson> list = donationPersonService.getDonationPersonList(status);
        return Result.success(list);
    }

    /**
     * 添加或更新捐赠人士
     */
    @PostMapping("/save")
    public Result<Void> saveOrUpdatePerson(@RequestBody DonationPerson person) {
        donationPersonService.saveOrUpdatePerson(person);
        return Result.success("保存成功");
    }

    /**
     * 删除捐赠人士
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePerson(@PathVariable Long id) {
        donationPersonService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 审核捐赠人士
     */
    @PostMapping("/review")
    public Result<Void> reviewPerson(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());

        DonationPerson person = donationPersonService.getById(id);
        if (person == null) {
            return Result.error("捐赠人士不存在");
        }
        person.setStatus(status);
        donationPersonService.updateById(person);
        return Result.success("审核成功");
    }

    /**
     * 统计捐赠人士信息
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countPersons() {
        Map<String, Object> result = donationPersonService.countPersons();
        return Result.success(result);
    }
}
