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
        // 从 Token 获取当前登录顾客ID，防止横向越权
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        List<CartItemVO> cartItems = cartService.getCartList(currentUserId);
        if (cartItems.isEmpty()) {
            return Result.error("购物车为空");
        }
        TradeOrder order = orderService.createOrder(currentUserId, cartItems);
        return Result.success(order);
    }

    @PostMapping("/pay")
    public Result<Void> payOrder(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo").toString();
        orderService.payOrder(orderNo);
        return Result.success("支付成功");
    }

    @GetMapping("/list")
    public Result<List<TradeOrder>> getOrderList() {
        // 顾客查看自己的订单（管理员查看全部订单请用 /order/all）
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        List<TradeOrder> orderList = orderService.getOrderList(currentUserId);
        return Result.success(orderList);
    }

    @GetMapping("/items/{orderId}")
    public Result<List<TradeOrderItem>> getOrderItems(@PathVariable Long orderId) {
        List<TradeOrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<TradeOrderItem>().eq(TradeOrderItem::getOrderId, orderId));
        return Result.success(items);
    }

    @GetMapping("/all")
    public Result<List<Map<String, Object>>> getAllOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {
        List<Map<String, Object>> orders = orderService.getAllOrders(orderNo, status);
        return Result.success(orders);
    }

    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Map<String, Object> detail = orderService.getOrderDetail(id);
        return Result.success(detail);
    }

    @PutMapping("/ship/{orderId}")
    public Result<Void> shipOrder(@PathVariable Long orderId) {
        orderService.shipOrder(orderId);
        return Result.success("发货成功");
    }

    @PutMapping("/complete/{orderId}")
    public Result<Void> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return Result.success("订单已完成");
    }

    @PutMapping("/cancel/{orderNo}")
    public Result<Void> cancelOrder(@PathVariable String orderNo) {
        orderService.cancelOrder(orderNo);
        return Result.success("订单已取消");
    }

    @PostMapping("/refund/apply")
    public Result<Void> applyRefund(@RequestBody Map<String, Object> params) {
        String orderNo = params.get("orderNo").toString();
        String reason = params.get("reason").toString();
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        orderService.applyRefund(orderNo, currentUserId, reason);
        return Result.success("退款申请已提交");
    }

    @GetMapping("/refund/list")
    public Result<List<TradeOrderRefund>> getRefundList(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Integer status) {
        List<TradeOrderRefund> refundList = orderService.getFilteredRefundList(userId, orderNo, status);
        return Result.success(refundList);
    }

    @PostMapping("/refund/handle")
    public Result<Void> handleRefund(@RequestBody Map<String, Object> params) {
        Object refundIdObj = params.get("refundId");
        Object statusObj = params.get("status");
        if (refundIdObj == null || statusObj == null) {
            return Result.error(400, "缺少必要参数");
        }
        try {
            Long refundId = Long.valueOf(refundIdObj.toString());
            Integer status = Integer.valueOf(statusObj.toString());
            String remark = params.get("remark") != null ? params.get("remark").toString() : null;
            orderService.handleRefund(refundId, status, remark);
            return Result.success("处理成功");
        } catch (NumberFormatException e) {
            return Result.error(400, "参数格式错误");
        }
    }
}
