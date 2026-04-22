package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.Inventory;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.InventoryMapper;
import com.shengwei.tushuguanli.service.InventoryService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存服务实现
 */
@Service
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory> implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    @Autowired
    @Lazy
    private com.shengwei.tushuguanli.service.BookInfoService bookInfoService;

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
        if (bookId == null) {
            throw new BusinessException("图书ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("锁定数量必须大于0");
        }

        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            log.error("[lockStock] 库存记录不存在, bookId={}, quantity={}", bookId, quantity);
            throw new BusinessException(String.format(
                    "图书[ID:%d]库存记录不存在，请联系管理员初始化库存", bookId));
        }

        // 检查可用库存
        if (inventory.getAvailableQuantity() < quantity) {
            log.warn("[lockStock] 可用库存不足, bookId={}, 需求={}, 可用={}",
                    bookId, quantity, inventory.getAvailableQuantity());
            throw new BusinessException(String.format(
                    "图书《%s》库存不足，当前可用 %d 本，需要 %d 本",
                    getBookNameSafe(bookId), inventory.getAvailableQuantity(), quantity));
        }

        // 使用 CAS 方式更新（乐观锁）：基于当前 availableQuantity 和 lockedQuantity 做条件更新，
        // 防止并发场景下超卖
        int rows = getBaseMapper().update(null, new LambdaUpdateWrapper<Inventory>()
                .eq(Inventory::getBookId, bookId)
                .eq(Inventory::getWarehouseCode, "DEFAULT")
                .eq(Inventory::getAvailableQuantity, inventory.getAvailableQuantity())   // CAS: 可用量未变
                .eq(Inventory::getLockedQuantity, inventory.getLockedQuantity())          // CAS: 锁定量未变
                .set(Inventory::getAvailableQuantity, inventory.getAvailableQuantity() - quantity)
                .set(Inventory::getLockedQuantity, inventory.getLockedQuantity() + quantity)
        );

        if (rows == 0) {
            // 并发冲突，重试一次
            log.warn("[lockStock] CAS更新失败(并发冲突), 正在重试, bookId={}, quantity={}", bookId, quantity);
            Inventory fresh = getInventoryByBookId(bookId);
            if (fresh == null) {
                throw new BusinessException(String.format(
                        "图书[ID:%d]库存记录不存在（并发删除），请刷新后重试", bookId));
            }
            if (fresh.getAvailableQuantity() < quantity) {
                throw new BusinessException(String.format(
                        "图书《%s》库存不足（并发抢占），当前可用 %d 本，需要 %d 本",
                        getBookNameSafe(bookId), fresh.getAvailableQuantity(), quantity));
            }
            // 重试
            int retryRows = getBaseMapper().update(null, new LambdaUpdateWrapper<Inventory>()
                    .eq(Inventory::getBookId, bookId)
                    .eq(Inventory::getWarehouseCode, "DEFAULT")
                    .set(Inventory::getAvailableQuantity, fresh.getAvailableQuantity() - quantity)
                    .set(Inventory::getLockedQuantity, fresh.getLockedQuantity() + quantity)
            );
            if (retryRows == 0) {
                throw new BusinessException("系统繁忙，库存锁定失败（并发冲突），请稍后重试");
            }
            log.info("[lockStock] 重试成功, bookId={}, quantity={}", bookId, quantity);
        } else {
            log.info("[lockStock] 库存锁定成功, bookId={}, quantity={}, 剩余可用={}",
                    bookId, quantity, inventory.getAvailableQuantity() - quantity);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseStock(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            log.error("[releaseStock] 库存记录不存在, bookId={}, quantity={}", bookId, quantity);
            throw new BusinessException(String.format(
                    "图书[ID:%d]库存记录不存在，无法释放库存", bookId));
        }

        if (inventory.getLockedQuantity() < quantity) {
            log.warn("[releaseStock] 锁定库存不足, bookId={}, 释放量={}, 已锁定={}",
                    bookId, quantity, inventory.getLockedQuantity());
            throw new BusinessException(String.format(
                    "图书《%s》锁定库存不足，已锁定 %d 本，需要释放 %d 本",
                    getBookNameSafe(bookId), inventory.getLockedQuantity(), quantity));
        }

        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantity);
        inventory.setLockedQuantity(inventory.getLockedQuantity() - quantity);
        updateById(inventory);
        log.info("[releaseStock] 库存释放成功, bookId={}, quantity={}, 可用={}",
                bookId, quantity, inventory.getAvailableQuantity() + quantity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmDeduction(Long bookId, Integer quantity) {
        Inventory inventory = getInventoryByBookId(bookId);
        if (inventory == null) {
            log.error("[confirmDeduction] 库存记录不存在, bookId={}, quantity={}", bookId, quantity);
            throw new BusinessException(String.format(
                    "图书[ID:%d]库存记录不存在，无法扣减库存", bookId));
        }

        if (inventory.getLockedQuantity() < quantity) {
            log.error("[confirmDeduction] 锁定库存不足, bookId={}, 扣减量={}, 已锁定={}",
                    bookId, quantity, inventory.getLockedQuantity());
            throw new BusinessException(String.format(
                    "图书《%s》锁定库存不足（数据异常），已锁定 %d 本，需要扣减 %d 本",
                    getBookNameSafe(bookId), inventory.getLockedQuantity(), quantity));
        }

        // 从锁定库存中扣除，同时减少总库存
        inventory.setLockedQuantity(inventory.getLockedQuantity() - quantity);
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);
        updateStockStatus(inventory);
        updateById(inventory);
        log.info("[confirmDeduction] 库存扣减成功, bookId={}, quantity={}, 总库存={}",
                bookId, quantity, inventory.getStockQuantity() - quantity);
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

    /**
     * 安全获取图书名称（用于错误信息展示）
     * 查询失败时返回占位文本，避免因图书查询异常导致原业务异常被掩盖
     */
    private String getBookNameSafe(Long bookId) {
        try {
            com.shengwei.tushuguanli.entity.BookInfo book = bookInfoService.getById(bookId);
            return book != null && book.getBookName() != null ? book.getBookName() : "ID:" + bookId;
        } catch (Exception e) {
            log.warn("[getBookNameSafe] 获取图书名称失败, bookId={}: {}", bookId, e.getMessage());
            return "ID:" + bookId;
        }
    }
}
