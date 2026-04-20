package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.service.MemberService;
import com.shengwei.tushuguanli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.unauthorized("未登录或登录已过期");
        }
        if (!currentUserId.equals(userId) && !hasAdminMemberPermission()) {
            return Result.forbidden("无权限访问");
        }
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

    private boolean hasAdminMemberPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("/admin/member".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof Long) {
            return (Long) authentication.getCredentials();
        }
        return null;
    }
}
