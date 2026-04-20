package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.CartItemVO;
import com.shengwei.tushuguanli.entity.TradeOrder;
import com.shengwei.tushuguanli.entity.TradeOrderItem;
import com.shengwei.tushuguanli.entity.TradeOrderRefund;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.TradeOrderMapper;
import com.shengwei.tushuguanli.mapper.TradeOrderItemMapper;
import com.shengwei.tushuguanli.mapper.TradeOrderRefundMapper;
import com.shengwei.tushuguanli.service.InventoryService;
import com.shengwei.tushuguanli.service.MemberService;
import com.shengwei.tushuguanli.service.ShoppingCartService;
import com.shengwei.tushuguanli.service.TradeOrderService;
import com.shengwei.tushuguanli.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单服务实现
 */
@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    @Autowired
    private ShoppingCartService cartService;
    
    @Autowired
    private TradeOrderItemMapper orderItemMapper;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private TradeOrderRefundMapper refundMapper;

    @Autowired
    private MemberService memberService;

    @Autowired
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(Long userId, List<CartItemVO> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        // 生成订单号
        String orderNo = generateOrderNo();
        System.out.println("创建订单，订单号：" + orderNo);

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemVO item : cartItems) {
            if (item.getSubtotal() != null) {
                totalAmount = totalAmount.add(item.getSubtotal());
            }
        }

        // 获取用户会员折扣
        com.shengwei.tushuguanli.entity.SysUser user = userService.getById(userId);
        int memberLevel = user != null && user.getMemberLevel() != null ? user.getMemberLevel() : 0;
        double discount = memberService.getDiscount(memberLevel);
        
        // 应用会员折扣
        BigDecimal payAmount = totalAmount.multiply(BigDecimal.valueOf(discount))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

        // 创建订单
        TradeOrder order = new TradeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(payAmount);
        order.setDiscount(discount);
        order.setMemberLevel(memberLevel);
        order.setStatus(0); // 0-待支付
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        save(order);

        // 创建订单明细 + 锁定库存
        for (CartItemVO item : cartItems) {
            // 锁定库存（下单时预占，防止超卖）
            inventoryService.lockStock(item.getBookId(), item.getQuantity());

            TradeOrderItem orderItem = new TradeOrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setBookId(item.getBookId());
            orderItem.setBookName(item.getBookName());
            orderItem.setPrice(item.getSellingPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSubtotal(item.getSubtotal());
            orderItem.setCreateTime(LocalDateTime.now());
            
            orderItemMapper.insert(orderItem);
        }

        // 清空购物车
        cartService.clearCart(userId);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo) {
        System.out.println("支付订单，订单号：" + orderNo);
        
        // 查询订单
        TradeOrder order = getOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderNo, orderNo));
        
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (order.getStatus() != 0) {
            System.out.println("订单已支付或已取消，订单号: " + orderNo);
            return;
        }
        
        // 更新订单状态为已支付
        LambdaQueryWrapper<TradeOrder> updateWrapper = new LambdaQueryWrapper<>();
        updateWrapper.eq(TradeOrder::getOrderNo, orderNo)
                     .eq(TradeOrder::getStatus, 0);
        
        TradeOrder updateEntity = new TradeOrder();
        updateEntity.setStatus(1); // 1-已支付
        updateEntity.setPaymentTime(LocalDateTime.now());
        updateEntity.setUpdateTime(LocalDateTime.now());
        
        boolean updated = update(updateEntity, updateWrapper);
        
        if (updated) {
            System.out.println("订单支付成功，订单号: " + orderNo);
            
            // 确认扣减锁定库存（支付成功，从锁定库存中真正扣除）
            try {
                List<TradeOrderItem> orderItems = orderItemMapper.selectList(
                        new LambdaQueryWrapper<TradeOrderItem>()
                                .eq(TradeOrderItem::getOrderId, order.getId()));
                for (TradeOrderItem item : orderItems) {
                    try {
                        inventoryService.confirmDeduction(item.getBookId(), item.getQuantity());
                    } catch (Exception e) {
                        System.out.println("扣减锁定库存异常: " + e.getMessage());
                    }
                }
            } catch (Exception e) {
                System.out.println("查询订单或扣减库存异常: " + e.getMessage());
            }
            
            // 更新会员信息（累计消费金额、积分、等级）
            try {
                memberService.updateMemberInfo(order.getUserId(), order.getPayAmount());
            } catch (Exception e) {
                System.out.println("更新会员信息异常: " + e.getMessage());
            }
        }
    }

    @Override
    public List<TradeOrder> getOrderList(Long userId) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(TradeOrder::getUserId, userId);
        }
        wrapper.orderByDesc(TradeOrder::getCreateTime);
        List<TradeOrder> orders = list(wrapper);
        
        // 为每个订单关联退款状态
        for (TradeOrder order : orders) {
            List<TradeOrderRefund> refunds = refundMapper.selectByOrderNo(order.getOrderNo());
            if (!refunds.isEmpty()) {
                TradeOrderRefund lastRefund = refunds.get(refunds.size() - 1);
                order.setRefundStatus(lastRefund.getStatus());
            } else {
                order.setRefundStatus(0);
            }
        }
        return orders;
    }

    @Override
    public List<Map<String, Object>> getAllOrders() {
        List<TradeOrder> orders = list(new LambdaQueryWrapper<TradeOrder>()
                .orderByDesc(TradeOrder::getCreateTime));
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (TradeOrder order : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("userId", order.getUserId());
            map.put("totalAmount", order.getTotalAmount());
            map.put("payAmount", order.getPayAmount());
            map.put("status", order.getStatus());
            map.put("paymentTime", order.getPaymentTime());
            map.put("createTime", order.getCreateTime());
            
            List<TradeOrderRefund> refunds = refundMapper.selectByOrderNo(order.getOrderNo());
            if (!refunds.isEmpty()) {
                TradeOrderRefund lastRefund = refunds.get(refunds.size() - 1);
                map.put("refundStatus", lastRefund.getStatus());
            } else {
                map.put("refundStatus", 0);
            }
            result.add(map);
        }
        
        return result;
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + (int)(Math.random() * 10000);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(String orderNo, Long userId, String reason) {
        TradeOrder order = getOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderNo, orderNo));
        
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        
        if (order.getStatus() != 1) {
            throw new BusinessException("只有已支付的订单才能申请退款");
        }

        List<TradeOrderRefund> existingRefunds = refundMapper.selectByOrderNo(orderNo);
        if (!existingRefunds.isEmpty()) {
            TradeOrderRefund lastRefund = existingRefunds.get(existingRefunds.size() - 1);
            if (lastRefund.getStatus() == 1) {
                throw new BusinessException("已有退款申请正在处理中");
            }
        }

        TradeOrderRefund refund = new TradeOrderRefund();
        refund.setOrderNo(orderNo);
        refund.setUserId(userId);
        refund.setAmount(order.getPayAmount());
        refund.setReason(reason);
        refund.setStatus(1); // 1-审核中
        refund.setCreateTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        
        refundMapper.insert(refund);
    }

    @Override
    public List<TradeOrderRefund> getRefundList(Long userId) {
        return refundMapper.selectByUserId(userId);
    }

    @Override
    public List<TradeOrderRefund> getAllRefundList(Integer status) {
        if (status != null && status >= 0) {
            return refundMapper.selectAllWithStatus(status);
        }
        return refundMapper.selectList(null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleRefund(Long refundId, Integer status, String remark) {
        TradeOrderRefund refund = refundMapper.selectById(refundId);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }
        
        if (refund.getStatus() != 1) {
            throw new BusinessException("该退款申请已处理");
        }

        refund.setStatus(status);
        refund.setRemark(remark);
        refund.setHandleTime(LocalDateTime.now());
        refund.setUpdateTime(LocalDateTime.now());
        
        refundMapper.updateById(refund);

        if (status == 2) {
            TradeOrder order = getOne(new LambdaQueryWrapper<TradeOrder>()
                    .eq(TradeOrder::getOrderNo, refund.getOrderNo()));
            if (order != null) {
                order.setStatus(4); // 4-已取消
                order.setUpdateTime(LocalDateTime.now());
                updateById(order);

                // 释放锁定库存（退款/取消时恢复可用库存）
                List<TradeOrderItem> orderItems = orderItemMapper.selectList(
                        new LambdaQueryWrapper<TradeOrderItem>()
                                .eq(TradeOrderItem::getOrderId, order.getId()));
                for (TradeOrderItem item : orderItems) {
                    try {
                        inventoryService.releaseStock(item.getBookId(), item.getQuantity());
                    } catch (Exception e) {
                        System.out.println("释放锁定库存异常: " + e.getMessage());
                    }
                }
            }
        }
    }
}
