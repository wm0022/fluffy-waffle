package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.shengwei.tushuguanli.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 订单服务实现
 */
@Service
public class TradeOrderServiceImpl extends ServiceImpl<TradeOrderMapper, TradeOrder> implements TradeOrderService {

    private static final Logger log = LoggerFactory.getLogger(TradeOrderServiceImpl.class);

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
    private CustomerService customerService;

    @Autowired
    private com.shengwei.tushuguanli.mapper.CustomerMapper customerMapper;

    @Autowired
    private com.shengwei.tushuguanli.service.BookInfoService bookInfoService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(Long userId, List<CartItemVO> cartItems) {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new BusinessException("购物车为空");
        }

        // 生成订单号
        String orderNo = generateOrderNo();
        log.info("[createOrder] 开始创建订单, userId={}, orderNo={}, 购物车商品数={}",
                userId, orderNo, cartItems.size());

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItemVO item : cartItems) {
            if (item.getSubtotal() != null) {
                totalAmount = totalAmount.add(item.getSubtotal());
            }
        }

        // 获取顾客会员折扣
        com.shengwei.tushuguanli.entity.Customer customer = customerService.getById(userId);
        int memberLevel = customer != null && customer.getMemberLevel() != null ? customer.getMemberLevel() : 0;
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
        log.info("[createOrder] 订单主表创建成功, orderId={}, orderNo={}", order.getId(), orderNo);

        // 创建订单明细 + 锁定库存（逐个锁定，某个失败则整体回滚）
        int successCount = 0;
        try {
            for (CartItemVO item : cartItems) {
                log.info("[createOrder] 锁定库存, bookId={}, bookName={}, quantity={}",
                        item.getBookId(), item.getBookName(), item.getQuantity());

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
                successCount++;
            }
        } catch (BusinessException e) {
            log.error("[createOrder] 库存锁定失败, 已锁定{}/{}项, bookId可能为第{}项商品, 原因: {}",
                    successCount, cartItems.size(), successCount + 1, e.getMessage());
            throw e; // @Transactional 自动回滚
        }

        // 清空购物车
        cartService.clearCart(userId);

        log.info("[createOrder] 订单创建完成, orderNo={}, 总计{}项商品", orderNo, cartItems.size());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(String orderNo) {
        log.info("[payOrder] 收到支付请求, orderNo={}", orderNo);
        
        // 查询订单
        TradeOrder order = getOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderNo, orderNo));
        
        if (order == null) {
            throw new BusinessException("订单不存在, orderNo=" + orderNo);
        }
        
        if (order.getStatus() != 0) {
            log.warn("[payOrder] 订单状态非待支付, orderNo={}, status={}", orderNo, order.getStatus());
            return;
        }
        
        // 更新订单状态为已支付
        LambdaUpdateWrapper<TradeOrder> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(TradeOrder::getOrderNo, orderNo)
                     .eq(TradeOrder::getStatus, 0);
        
        TradeOrder updateEntity = new TradeOrder();
        updateEntity.setStatus(1); // 1-已支付
        updateEntity.setPaymentTime(LocalDateTime.now());
        updateEntity.setUpdateTime(LocalDateTime.now());
        
        boolean updated = update(updateEntity, updateWrapper);
        
        if (updated) {
            log.info("[payOrder] 订单支付成功, orderNo={}, orderId={}", orderNo, order.getId());
            
            // 确认扣减锁定库存（支付成功，从锁定库存中真正扣除）
            try {
                List<TradeOrderItem> orderItems = orderItemMapper.selectList(
                        new LambdaQueryWrapper<TradeOrderItem>()
                                .eq(TradeOrderItem::getOrderId, order.getId()));
                for (TradeOrderItem item : orderItems) {
                    try {
                        inventoryService.confirmDeduction(item.getBookId(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("[payOrder] 扣减锁定库存异常, bookId={}, quantity={}, orderId={}: {}",
                                item.getBookId(), item.getQuantity(), order.getId(), e.getMessage(), e);
                        // 不再静默吞掉，向上抛出触发事务回滚
                        throw new BusinessException(
                                String.format("图书《%s》库存扣减失败: %s", item.getBookName(), e.getMessage()));
                    }
                }

                // 同步更新 book_info 表的库存和销量
                for (TradeOrderItem item : orderItems) {
                    try {
                        com.shengwei.tushuguanli.entity.BookInfo bookInfo = bookInfoService.getById(item.getBookId());
                        if (bookInfo != null) {
                            com.shengwei.tushuguanli.entity.Inventory inv = inventoryService.getInventoryByBookId(item.getBookId());
                            if (inv != null) {
                                bookInfo.setStockCount(inv.getStockQuantity());
                            }
                            int oldSales = bookInfo.getSalesVolume() != null ? bookInfo.getSalesVolume() : 0;
                            bookInfo.setSalesVolume(oldSales + item.getQuantity());
                            bookInfoService.updateById(bookInfo);
                        }
                    } catch (Exception e) {
                        log.error("[payOrder] 同步book_info库存/销量异常, bookId={}, orderId={}: {}",
                                item.getBookId(), order.getId(), e.getMessage(), e);
                    }
                }
            } catch (BusinessException be) {
                throw be; // 业务异常继续上抛
            } catch (Exception e) {
                log.error("[payOrder] 支付后处理异常, orderNo={}: {}", orderNo, e.getMessage(), e);
                throw new BusinessException("订单支付成功但后处理异常: " + e.getMessage());
            }
            
            // 更新会员信息（累计消费金额、积分、等级）
            try {
                memberService.updateMemberInfo(order.getUserId(), order.getPayAmount());
            } catch (Exception e) {
                log.warn("[payOrder] 更新会员信息异常（不影响订单）, userId={}, amount={}: {}",
                        order.getUserId(), order.getPayAmount(), e.getMessage());
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
    public List<Map<String, Object>> getAllOrders(String orderNo, Integer status) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<TradeOrder>();
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(TradeOrder::getOrderNo, orderNo);
        }
        if (status != null) {
            wrapper.eq(TradeOrder::getStatus, status);
        }
        wrapper.orderByDesc(TradeOrder::getCreateTime);
        List<TradeOrder> orders = list(wrapper);

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

    @Override
    public Map<String, Object> getOrderDetail(Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        List<TradeOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<TradeOrderItem>().eq(TradeOrderItem::getOrderId, orderId));

        // 查询顾客信息
        com.shengwei.tushuguanli.entity.Customer customer = customerMapper.selectById(order.getUserId());
        Map<String, Object> userInfo = null;
        if (customer != null) {
            userInfo = new HashMap<>();
            userInfo.put("realName", customer.getRealName() != null ? customer.getRealName() : customer.getUsername());
            userInfo.put("address", customer.getAddress());
            userInfo.put("phone", customer.getPhone());
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("order", order);
        detail.put("items", items);
        detail.put("user", userInfo);

        List<TradeOrderRefund> refunds = refundMapper.selectByOrderNo(order.getOrderNo());
        detail.put("refunds", refunds.isEmpty() ? null : refunds);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("只有已支付的订单才能发货");
        }
        TradeOrder updateEntity = new TradeOrder();
        updateEntity.setId(orderId);
        updateEntity.setStatus(2); // 2-已发货
        updateEntity.setUpdateTime(LocalDateTime.now());
        updateById(updateEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void completeOrder(Long orderId) {
        TradeOrder order = getById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("只有已发货的订单才能完成");
        }
        TradeOrder updateEntity = new TradeOrder();
        updateEntity.setId(orderId);
        updateEntity.setStatus(3); // 3-已完成
        updateEntity.setUpdateTime(LocalDateTime.now());
        updateById(updateEntity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(String orderNo) {
        log.info("[cancelOrder] 收到取消订单请求, orderNo={}", orderNo);
        TradeOrder order = getOne(new LambdaQueryWrapper<TradeOrder>()
                .eq(TradeOrder::getOrderNo, orderNo));
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只有待支付的订单才能取消");
        }
        // 释放锁定库存
        try {
            List<TradeOrderItem> orderItems = orderItemMapper.selectList(
                    new LambdaQueryWrapper<TradeOrderItem>().eq(TradeOrderItem::getOrderId, order.getId()));
            for (TradeOrderItem item : orderItems) {
                inventoryService.releaseStock(item.getBookId(), item.getQuantity());
            }

            // 同步恢复 book_info 表的库存
            for (TradeOrderItem item : orderItems) {
                try {
                    com.shengwei.tushuguanli.entity.BookInfo bookInfo = bookInfoService.getById(item.getBookId());
                    if (bookInfo != null) {
                        com.shengwei.tushuguanli.entity.Inventory inv = inventoryService.getInventoryByBookId(item.getBookId());
                        if (inv != null) {
                            bookInfo.setStockCount(inv.getStockQuantity());
                            bookInfoService.updateById(bookInfo);
                        }
                    }
                } catch (Exception e) {
                    log.warn("[cancelOrder] 同步book_info库存异常, bookId={}, orderId={}: {}",
                            item.getBookId(), order.getId(), e.getMessage());
                }
            }
            log.info("[cancelOrder] 库存释放完成, orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("[cancelOrder] 释放库存异常, orderNo={}: {}", orderNo, e.getMessage(), e);
            throw new BusinessException("取消订单释放库存失败: " + e.getMessage());
        }
        TradeOrder updateEntity = new TradeOrder();
        updateEntity.setId(order.getId());
        updateEntity.setStatus(4); // 4-已取消
        updateEntity.setUpdateTime(LocalDateTime.now());
        updateById(updateEntity);
        log.info("[cancelOrder] 订单已取消, orderNo={}", orderNo);
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
        return getFilteredRefundList(null, null, status);
    }

    @Override
    public List<TradeOrderRefund> getFilteredRefundList(Long userId, String orderNo, Integer status) {
        LambdaQueryWrapper<TradeOrderRefund> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(TradeOrderRefund::getUserId, userId);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.like(TradeOrderRefund::getOrderNo, orderNo);
        }
        if (status != null && status >= 0) {
            wrapper.eq(TradeOrderRefund::getStatus, status);
        }
        wrapper.orderByDesc(TradeOrderRefund::getCreateTime);
        return refundMapper.selectList(wrapper);
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

                // 获取订单明细
                List<TradeOrderItem> orderItems = orderItemMapper.selectList(
                        new LambdaQueryWrapper<TradeOrderItem>()
                                .eq(TradeOrderItem::getOrderId, order.getId()));

                // 安全释放库存：直接操作数据库，不调用可能抛异常的 service 方法
                // 注意：支付后 confirmDeduction 已将 locked 清零并扣减 stockQuantity，
                //       所以退款时需要同时增加 available 和 totalStock
                for (TradeOrderItem item : orderItems) {
                    try {
                        com.shengwei.tushuguanli.entity.Inventory inv = inventoryService.getInventoryByBookId(item.getBookId());
                        if (inv != null) {
                            inv.setAvailableQuantity(inv.getAvailableQuantity() + item.getQuantity());
                            inv.setStockQuantity(inv.getStockQuantity() + item.getQuantity());
                            inventoryService.updateById(inv);
                            log.info("[handleRefund] 库存回增成功: bookId={}, quantity={}, 可用={}",
                                    item.getBookId(), item.getQuantity(), inv.getAvailableQuantity());
                        } else {
                            log.warn("[handleRefund] 库存记录不存在，跳过释放: bookId={}, quantity={}",
                                    item.getBookId(), item.getQuantity());
                        }
                    } catch (Exception e) {
                        log.error("[handleRefund] 释放库存失败: bookId={}, error={}", item.getBookId(), e.getMessage());
                    }
                }

                // 同步更新 book_info 表
                for (TradeOrderItem item : orderItems) {
                    try {
                        com.shengwei.tushuguanli.entity.BookInfo bookInfo = bookInfoService.getById(item.getBookId());
                        if (bookInfo != null) {
                            com.shengwei.tushuguanli.entity.Inventory inv = inventoryService.getInventoryByBookId(item.getBookId());
                            if (inv != null) {
                                bookInfo.setStockCount(inv.getStockQuantity());
                            }
                            int oldSales = bookInfo.getSalesVolume() != null ? bookInfo.getSalesVolume() : 0;
                            bookInfo.setSalesVolume(Math.max(0, oldSales - item.getQuantity()));
                            bookInfoService.updateById(bookInfo);
                        }
                    } catch (Exception e) {
                        log.error("[handleRefund] 同步book_info失败: bookId={}, error={}", item.getBookId(), e.getMessage());
                    }
                }
            }
        }
    }
}
