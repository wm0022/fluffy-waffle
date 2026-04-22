package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.Customer;
import com.shengwei.tushuguanli.mapper.CustomerMapper;
import com.shengwei.tushuguanli.service.MemberService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 会员服务实现
 */
@Service
public class MemberServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements MemberService {

    @Override
    public int calculateMemberLevel(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return 0;
        }
        BigDecimal amount = totalAmount;
        if (amount.compareTo(new BigDecimal("2888")) >= 0) {
            return 4; // 钻石卡会员
        } else if (amount.compareTo(new BigDecimal("1888")) >= 0) {
            return 3; // 金卡会员
        } else if (amount.compareTo(new BigDecimal("388")) >= 0) {
            return 2; // 银卡会员
        } else if (amount.compareTo(new BigDecimal("188")) >= 0) {
            return 1; // 普通会员
        } else {
            return 0; // 非会员
        }
    }

    @Override
    public double getDiscount(int memberLevel) {
        switch (memberLevel) {
            case 1: return 0.95; // 普通会员 95折
            case 2: return 0.90; // 银卡会员 9折
            case 3: return 0.85; // 金卡会员 85折
            case 4: return 0.80; // 钻石卡会员 8折
            default: return 1.0; // 非会员无折扣
        }
    }

    @Override
    public String getLevelName(int memberLevel) {
        switch (memberLevel) {
            case 1: return "普通会员";
            case 2: return "银卡会员";
            case 3: return "金卡会员";
            case 4: return "钻石卡会员";
            default: return "非会员";
        }
    }

    @Override
    public int calculatePoints(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        return amount.intValue(); // 1元=1积分
    }

    @Override
    public void updateMemberInfo(Long userId, BigDecimal payAmount) {
        Customer user = getById(userId);
        if (user == null) {
            return;
        }

        BigDecimal currentTotal = user.getTotalAmount() != null ? user.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal newTotal = currentTotal.add(payAmount != null ? payAmount : BigDecimal.ZERO);
        int currentPoints = user.getPoints() != null ? user.getPoints() : 0;
        int newPoints = currentPoints + calculatePoints(payAmount);
        int newLevel = calculateMemberLevel(newTotal);

        user.setTotalAmount(newTotal);
        user.setPoints(newPoints);
        user.setMemberLevel(newLevel);
        user.setUpdateTime(java.time.LocalDateTime.now());

        updateById(user);
    }

    @Override
    public Map<String, Object> getMemberInfo(Long userId) {
        Customer user = getById(userId);
        Map<String, Object> result = new HashMap<>();
        if (user == null) {
            result.put("memberLevel", 0);
            result.put("levelName", "非会员");
            result.put("points", 0);
            result.put("totalAmount", BigDecimal.ZERO);
            result.put("discount", 1.0);
        } else {
            int level = user.getMemberLevel() != null ? user.getMemberLevel() : 0;
            result.put("memberLevel", level);
            result.put("levelName", getLevelName(level));
            result.put("points", user.getPoints() != null ? user.getPoints() : 0);
            result.put("totalAmount", user.getTotalAmount() != null ? user.getTotalAmount() : BigDecimal.ZERO);
            result.put("discount", getDiscount(level));
            result.put("nextLevel", getNextLevelInfo(level));
        }
        return result;
    }

    private Map<String, Object> getNextLevelInfo(int currentLevel) {
        Map<String, Object> info = new HashMap<>();
        switch (currentLevel) {
            case 0:
                info.put("nextLevel", "普通会员");
                info.put("needAmount", 188);
                break;
            case 1:
                info.put("nextLevel", "银卡会员");
                info.put("needAmount", 388);
                break;
            case 2:
                info.put("nextLevel", "金卡会员");
                info.put("needAmount", 1888);
                break;
            case 3:
                info.put("nextLevel", "钻石卡会员");
                info.put("needAmount", 2888);
                break;
            case 4:
                info.put("nextLevel", "最高等级");
                info.put("needAmount", 0);
                break;
            default:
                info.put("nextLevel", "普通会员");
                info.put("needAmount", 188);
        }
        return info;
    }
}
