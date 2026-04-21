package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.Inventory;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.InventoryMapper;
import com.shengwei.tushuguanli.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存服务实现
 */
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInventory(Long bookId, Integer quantity) {
        Inventory inventory = new Inventory();
        inventory.setBookId(bookId);
        inventory.setStockQuantity(quantity);
        inventory.setAvailableQuantity(quantity);
        inventory.setLockedQuantity(0);
        inventory.setWarehouseCode("DEFAULT");
        inventory.setWarehouseName("主仓库");
        inventory.setMinStock(10);
        inventory.setMaxStock(1000);
        inventory.setStockStatus(quantity <= 10 ? 3 : (quantity <= 50 ? 2 : 1));
        save(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByBookId(Long bookId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getBookId, bookId);
        remove(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseStock(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            createInventory(bookId, quantity);
            return;
        }

        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        updateStockStatus(inventory);
        updateById(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void decreaseStock(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            // 演示系统：如果库存不存在，创建库存并设置为0
            createInventory(bookId, 0);
            inventory = getInventoryByBookId(bookId);
        }

        // 演示系统：直接减少库存，允许负数
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        updateStockStatus(inventory);
        updateById(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockStock(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            throw new BusinessException("库存不存在");
        }

        if (inventory.getAvailableQuantity() < quantity) {
            throw new BusinessException("可用库存不足");
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() - quantity);
        inventory.setLockedQuantity(inventory.getLockedQuantity() + quantity);
        updateById(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            throw new BusinessException("库存不存在");
        }

        if (inventory.getLockedQuantity() < quantity) {
            throw new BusinessException("锁定库存不足");
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setLockedQuantity(inventory.getLockedQuantity() - quantity);
        updateById(inventory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDeduction(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            throw new BusinessException("库存不存在");
        }

        if (inventory.getLockedQuantity() < quantity) {
            throw new BusinessException("锁定库存不足，无法扣减");
        }

        // 从锁定库存中扣除，同时减少总库存
        inventory.setLockedQuantity(inventory.getLockedQuantity() - quantity);
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        updateStockStatus(inventory);
        updateById(inventory);
    }

    @Override
    public boolean checkStock(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        return inventory != null && inventory.getAvailableQuantity() >= quantity;
    }

    @Override
    public Integer getAvailableStock(Long bookId) {
        Inventory inventory = getInventoryByBookId(bookId);
        return inventory != null ? inventory.getAvailableQuantity() : 0;
    }

    @Override
    public Inventory getInventoryByBookId(Long bookId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getBookId, bookId)
                .eq(Inventory::getWarehouseCode, "DEFAULT");
        return getOne(wrapper);
    }

    private void updateStockStatus(Inventory inventory) {
        Integer stock = inventory.getStockQuantity();
        if (stock <= 0) {
            inventory.setStockStatus(3);
        } else if (stock <= inventory.getMinStock()) {
            inventory.setStockStatus(2);
        } else {
            inventory.setStockStatus(1);
        }
    }
}
