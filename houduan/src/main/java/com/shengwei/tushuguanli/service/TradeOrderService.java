package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.CartItemVO;
import com.shengwei.tushuguanli.entity.TradeOrder;
import com.shengwei.tushuguanli.entity.TradeOrderRefund;
import java.util.List;
import java.util.Map;

/**
 * 订单服务
 */
public interface TradeOrderService extends IService<TradeOrder> {
    
    /**
     * 创建订单
     */
    TradeOrder createOrder(Long userId, List<CartItemVO> cartItems);
    
    /**
     * 支付订单
     */
    void payOrder(String orderNo);
    
    /**
     * 获取用户订单列表
     */
    List<TradeOrder> getOrderList(Long userId);
    
    /**
     * 获取所有订单（管理员）
     */
    List<Map<String, Object>> getAllOrders(String orderNo, Integer status);

    /**
     * 获取订单详情（含明细）
     */
    Map<String, Object> getOrderDetail(Long orderId);

    /**
     * 发货
     */
    void shipOrder(Long orderId);

    /**
     * 完成订单
     */
    void completeOrder(Long orderId);

    /**
     * 取消待支付订单
     */
    void cancelOrder(String orderNo);

    /**
     * 申请退款
     */
    void applyRefund(String orderNo, Long userId, String reason);

    /**
     * 获取用户退款列表
     */
    List<TradeOrderRefund> getRefundList(Long userId);

    /**
     * 获取所有退款申请（管理员）
     */
    List<TradeOrderRefund> getAllRefundList(Integer status);

    /**
     * 筛选退款列表（支持多条件）
     */
    List<TradeOrderRefund> getFilteredRefundList(Long userId, String orderNo, Integer status);

    /**
     * 处理退款申请
     */
    void handleRefund(Long refundId, Integer status, String remark);
}
