package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.service.MemberService;
import com.shengwei.tushuguanli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private UserService userService;

    @Autowired
    private MemberService memberService;

    @GetMapping("/info")
    public Result<Map<String, Object>> getMemberInfo(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        SysUser user = userService.getById(userId);
        if (user != null) {
            int memberLevel = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
            int points = user.getPoints() != null ? user.getPoints() : 0;
            java.math.BigDecimal totalAmount = user.getTotalAmount() != null ? user.getTotalAmount() : java.math.BigDecimal.ZERO;
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
