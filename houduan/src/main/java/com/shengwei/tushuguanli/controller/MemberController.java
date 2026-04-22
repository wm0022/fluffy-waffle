package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.Customer;
import com.shengwei.tushuguanli.service.CustomerService;
import com.shengwei.tushuguanli.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private MemberService memberService;

    @GetMapping("/info")
    public Result<Map<String, Object>> getMemberInfo() {
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        Map<String, Object> result = new HashMap<>();
        Customer customer = customerService.getById(currentUserId);
        if (customer != null) {
            int memberLevel = customer.getMemberLevel() != null ? customer.getMemberLevel() : 0;
            int points = customer.getPoints() != null ? customer.getPoints() : 0;
            java.math.BigDecimal totalAmount = customer.getTotalAmount() != null ? customer.getTotalAmount() : java.math.BigDecimal.ZERO;
            double discount = memberService.getDiscount(memberLevel);
            String levelName = memberService.getLevelName(memberLevel);
            
            result.put("memberLevel", memberLevel);
            result.put("levelName", levelName);
            result.put("points", points);
            result.put("totalAmount", totalAmount);
            result.put("discount", discount);
            result.put("discountText", memberLevel == 0 ? "无折扣" : String.format("%.2f折", discount * 10));
        }
        return Result.success(result);
    }
}
