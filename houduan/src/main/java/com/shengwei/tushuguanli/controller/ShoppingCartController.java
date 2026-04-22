package com.shengwei.tushuguanli.controller;

import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.CartItemVO;
import com.shengwei.tushuguanli.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 */
@RestController
@RequestMapping("/cart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService cartService;

    /**
     * 添加到购物车
     */
    @PostMapping("/add")
    public Result<Void> addToCart(@RequestBody Map<String, Object> params) {
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        Long bookId = Long.valueOf(params.get("bookId").toString());
        Integer quantity = Integer.valueOf(params.get("quantity").toString());
        
        cartService.addToCart(currentUserId, bookId, quantity);
        return Result.success("添加成功");
    }

    /**
     * 获取购物车列表
     */
    @GetMapping("/list")
    public Result<List<CartItemVO>> getCartList() {
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        List<CartItemVO> cartList = cartService.getCartList(currentUserId);
        return Result.success(cartList);
    }

    /**
     * 从购物车删除
     */
    @DeleteMapping("/remove")
    public Result<Void> removeFromCart(@RequestBody Map<String, Object> params) {
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        Long bookId = Long.valueOf(params.get("bookId").toString());
        
        cartService.removeFromCart(currentUserId, bookId);
        return Result.success("删除成功");
    }

    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public Result<Void> clearCart() {
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        cartService.clearCart(currentUserId);
        return Result.success("清空成功");
    }
}
