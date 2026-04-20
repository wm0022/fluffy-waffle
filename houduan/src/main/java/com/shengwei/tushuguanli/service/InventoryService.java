package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.Inventory;

/**
 * 库存服务
 */
public interface InventoryService extends IService<Inventory> {

    /**
     * 创建库存
     */
    void createInventory(Long bookId, Integer quantity);

    /**
     * 删除图书库存
     */
    void deleteByBookId(Long bookId);

    /**
     * 增加库存
     */
    void increaseStock(Long bookId, Integer quantity);

    /**
     * 减少库存
     */
    void decreaseStock(Long bookId, Integer quantity);

    /**
     * 锁定库存
     */
    void lockStock(Long bookId, Integer quantity);

    /**
     * 释放锁定库存
     */
    void releaseStock(Long bookId, Integer quantity);

    /**
     * 确认扣减锁定库存（支付成功后调用，从锁定库存中真正扣除）
     */
    void confirmDeduction(Long bookId, Integer quantity);

    /**
     * 检查库存是否充足
     */
    boolean checkStock(Long bookId, Integer quantity);

    /**
     * 获取可用库存
     */
    Integer getAvailableStock(Long bookId);
}
