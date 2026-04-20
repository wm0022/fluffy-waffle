package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.CartItemVO;
import com.shengwei.tushuguanli.entity.ShoppingCart;
import java.util.List;

/**
 * 购物车服务
 */
public interface ShoppingCartService extends IService<ShoppingCart> {
    
    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, Long bookId, Integer quantity);
    
    /**
     * 获取用户购物车列表
     */
    List<CartItemVO> getCartList(Long userId);
    
    /**
     * 清空购物车
     */
    void clearCart(Long userId);
    
    /**
     * 从购物车删除商品
     */
    void removeFromCart(Long userId, Long bookId);
}
