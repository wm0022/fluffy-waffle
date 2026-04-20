package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.CartItemVO;
import com.shengwei.tushuguanli.entity.TradeOrder;
import com.shengwei.tushuguanli.entity.TradeOrderItem;
import com.shengwei.tushuguanli.entity.TradeOrderRefund;
import com.shengwei.tushuguanli.mapper.TradeOrderItemMapper;
import com.shengwei.tushuguanli.service.ShoppingCartService;
import com.shengwei.tushuguanli.service.TradeOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class TradeOrderController {

    @Autowired
    private TradeOrderService orderService;

    @Autowired
    private ShoppingCartService cartService;

    @Autowired
    private TradeOrderItemMapper orderItemMapper;

    @PostMapping("/create")
    public Result<TradeOrder> createOrder(@RequestBody Map<String, Object> params) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("未登录或登录已过期");
        }
        List<CartItemVO> cartItems = cartService.getCartList(userId);
        if (cartItems.isEmpty()) {
            return Result.error("购物车为空");
        }
        TradeOrder order = orderService.createOrder(userId, cartItems);
        return Result.success(order);
    }

    @PostMapping("/pay")
    public Result<Void> payOrder(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo").toString();
        orderService.payOrder(orderNo);
        return Result.success("支付成功");
    }

    @GetMapping("/list")
    public Result<List<TradeOrder>> getOrderList(@RequestParam(required = false) Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.unauthorized("未登录或登录已过期");
        }
        if (userId != null && !currentUserId.equals(userId) && !hasAdminOrderPermission()) {
            return Result.forbidden("无权限访问");
        }
        if (userId == null && !hasAdminOrderPermission()) {
            userId = currentUserId;
        }
        List<TradeOrder> orderList = orderService.getOrderList(userId);
        return Result.success(orderList);
    }

    @GetMapping("/items/{orderId}")
    public Result<List<TradeOrderItem>> getOrderItems(@PathVariable Long orderId) {
        List<TradeOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<TradeOrderItem>().eq(TradeOrderItem::getOrderId, orderId));
        return Result.success(items);
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllOrders() {
        List<Map<String, Object>> orders = orderService.getAllOrders();
        return Result.success(orders);
    }

    @PostMapping("/refund/apply")
    public Result<Void> applyRefund(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo").toString();
        String reason = params.get("reason").toString();
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Result.unauthorized("未登录或登录已过期");
        }
        orderService.applyRefund(orderNo, userId, reason);
        return Result.success("退款申请已提交");
    }

    @GetMapping("/refund/list")
    public Result<List<TradeOrderRefund>> getRefundList(@RequestParam(required = false) Long userId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.unauthorized("未登录或登录已过期");
        }
        if (userId != null) {
            if (!currentUserId.equals(userId) && !hasAdminOrderPermission()) {
                return Result.forbidden("无权限访问");
            }
            return Result.success(orderService.getRefundList(userId));
        }
        if (hasAdminOrderPermission()) {
            return Result.success(orderService.getAllRefundList(null));
        }
        return Result.success(orderService.getRefundList(currentUserId));
    }

    @PostMapping("/refund/handle")
    public Result<Void> handleRefund(@RequestBody Map<String, Object> params) {
        Long refundId = Long.valueOf(params.get("refundId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String remark = params.get("remark") != null ? params.get("remark").toString() : null;
        orderService.handleRefund(refundId, status, remark);
        return Result.success("处理成功");
    }

    private boolean hasAdminOrderPermission() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getAuthorities() == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("/admin/order".equals(authority.getAuthority())) {
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
