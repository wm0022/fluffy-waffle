package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.Inventory;
import com.shengwei.tushuguanli.entity.TradeOrder;
import com.shengwei.tushuguanli.entity.TradeOrderItem;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.mapper.InventoryMapper;
import com.shengwei.tushuguanli.mapper.TradeOrderItemMapper;
import com.shengwei.tushuguanli.mapper.TradeOrderMapper;
import com.shengwei.tushuguanli.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private TradeOrderMapper orderMapper;

    @Autowired
    private TradeOrderItemMapper orderItemMapper;

    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list() {
        List<Inventory> inventories = inventoryService.list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Inventory inv : inventories) {
            BookInfo book = bookInfoMapper.selectById(inv.getBookId());
            if (book == null) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("inventoryId", inv.getInventoryId());
            map.put("bookId", inv.getBookId());
            map.put("stockQuantity", inv.getStockQuantity());
            map.put("availableQuantity", inv.getAvailableQuantity());
            map.put("lockedQuantity", inv.getLockedQuantity());
            map.put("minStock", inv.getMinStock());
            map.put("maxStock", inv.getMaxStock());
            map.put("stockStatus", inv.getStockStatus());
            map.put("bookName", book.getBookName());
            map.put("author", book.getAuthor());
            map.put("sellingPrice", book.getSellingPrice());

            result.add(map);
        }

        return Result.success(result);
    }

    @GetMapping("/warning-count")
    public Result<Integer> getWarningCount() {
        List<Inventory> allInventories = inventoryService.list();
        long count = allInventories.stream()
                .filter(inv -> {
                    int stock = inv.getStockQuantity() != null ? inv.getStockQuantity() : 0;
                    int minStock = inv.getMinStock() != null ? inv.getMinStock() : 10;
                    return stock <= minStock;
                })
                .count();
        return Result.success((int) count);
    }

    @GetMapping("/warning")
    public Result<List<Map<String, Object>>> getWarningList() {
        List<Inventory> allInventories = inventoryService.list();
        List<Inventory> warningInventories = new ArrayList<>();
        
        for (Inventory inv : allInventories) {
            int stock = inv.getStockQuantity() != null ? inv.getStockQuantity() : 0;
            int minStock = inv.getMinStock() != null ? inv.getMinStock() : 10;
            if (stock <= minStock) {
                warningInventories.add(inv);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Inventory inv : warningInventories) {
            BookInfo book = bookInfoMapper.selectById(inv.getBookId());
            if (book == null) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("inventoryId", inv.getInventoryId());
            map.put("bookId", inv.getBookId());
            map.put("stockQuantity", inv.getStockQuantity() != null ? inv.getStockQuantity() : 0);
            map.put("minStock", inv.getMinStock());
            map.put("availableQuantity", inv.getAvailableQuantity());
            map.put("bookName", book.getBookName());
            map.put("author", book.getAuthor());
            map.put("sellingPrice", book.getSellingPrice());

            int shortage = inv.getMinStock() - (inv.getStockQuantity() != null ? inv.getStockQuantity() : 0);
            map.put("shortage", Math.max(shortage, 0));

            if (inv.getStockQuantity() != null && inv.getStockQuantity() <= 0) {
                map.put("warningLevel", "严重缺货");
            } else if (inv.getStockQuantity() != null && inv.getStockQuantity() <= inv.getMinStock() / 2) {
                map.put("warningLevel", "库存紧张");
            } else {
                map.put("warningLevel", "库存预警");
            }

            result.add(map);
        }

        return Result.success(result);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getSalesStatistics(
            @RequestParam(defaultValue = "7") Integer days) {
        
        LocalDate startDate = LocalDate.now().minusDays(days);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = LocalDateTime.now();

        LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(TradeOrder::getCreateTime, startDateTime)
                .le(TradeOrder::getCreateTime, endDateTime)
                .eq(TradeOrder::getStatus, 1);
        List<TradeOrder> orders = orderMapper.selectList(orderWrapper);

        Map<Long, Integer> bookSalesMap = new HashMap<>();
        Map<Long, BigDecimal> bookRevenueMap = new HashMap<>();
        Map<Long, String> bookNameMap = new HashMap<>();
        Map<Long, String> bookAuthorMap = new HashMap<>();
        int totalSales = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (TradeOrder order : orders) {
            LambdaQueryWrapper<TradeOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(TradeOrderItem::getOrderId, order.getId());
            List<TradeOrderItem> items = orderItemMapper.selectList(itemWrapper);

            for (TradeOrderItem item : items) {
                bookSalesMap.merge(item.getBookId(), item.getQuantity(), Integer::sum);
                BigDecimal revenue = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                bookRevenueMap.merge(item.getBookId(), revenue, BigDecimal::add);
                if (!bookNameMap.containsKey(item.getBookId())) {
                    bookNameMap.put(item.getBookId(), item.getBookName() != null ? item.getBookName() : "未知图书");
                }
                totalSales += item.getQuantity();
                totalRevenue = totalRevenue.add(revenue);
            }
        }

        // 尝试从book_info表补充作者信息
        for (Long bookId : bookSalesMap.keySet()) {
            BookInfo book = bookInfoMapper.selectById(bookId);
            if (book != null) {
                bookNameMap.put(bookId, book.getBookName());
                bookAuthorMap.put(bookId, book.getAuthor());
            }
        }

        List<Map<String, Object>> topBooks = bookSalesMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    Long bookId = entry.getKey();
                    map.put("bookId", bookId);
                    map.put("bookName", bookNameMap.getOrDefault(bookId, "未知图书"));
                    map.put("salesVolume", entry.getValue());
                    map.put("revenue", bookRevenueMap.get(bookId));
                    return map;
                })
                .collect(Collectors.toList());

        List<Map<String, Object>> dailySales = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            LambdaQueryWrapper<TradeOrder> dayWrapper = new LambdaQueryWrapper<>();
            dayWrapper.ge(TradeOrder::getCreateTime, dayStart)
                    .lt(TradeOrder::getCreateTime, dayEnd)
                    .eq(TradeOrder::getStatus, 1);
            List<TradeOrder> dayOrders = orderMapper.selectList(dayWrapper);

            int daySales = 0;
            BigDecimal dayRevenue = BigDecimal.ZERO;
            for (TradeOrder order : dayOrders) {
                LambdaQueryWrapper<TradeOrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(TradeOrderItem::getOrderId, order.getId());
                List<TradeOrderItem> items = orderItemMapper.selectList(itemWrapper);
                for (TradeOrderItem item : items) {
                    daySales += item.getQuantity();
                    dayRevenue = dayRevenue.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }

            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", date.toString());
            dayMap.put("salesVolume", daySales);
            dayMap.put("revenue", dayRevenue);
            dailySales.add(dayMap);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalSales", totalSales);
        result.put("totalRevenue", totalRevenue);
        result.put("topBooks", topBooks);
        result.put("dailySales", dailySales);
        result.put("periodDays", days);

        return Result.success(result);
    }

    @GetMapping("/restock-suggestions")
    public Result<List<Map<String, Object>>> getRestockSuggestions() {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        LocalDateTime thirtyDaysAgoStart = thirtyDaysAgo.atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(TradeOrder::getCreateTime, thirtyDaysAgoStart)
                .le(TradeOrder::getCreateTime, now)
                .eq(TradeOrder::getStatus, 1);
        List<TradeOrder> orders = orderMapper.selectList(orderWrapper);

        Map<Long, Integer> bookSalesMap = new HashMap<>();
        for (TradeOrder order : orders) {
            LambdaQueryWrapper<TradeOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(TradeOrderItem::getOrderId, order.getId());
            List<TradeOrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (TradeOrderItem item : items) {
                bookSalesMap.merge(item.getBookId(), item.getQuantity(), Integer::sum);
            }
        }

        List<Inventory> allInventories = inventoryService.list();
        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (Inventory inv : allInventories) {
            BookInfo book = bookInfoMapper.selectById(inv.getBookId());
            if (book == null) continue;

            Integer salesVolume = bookSalesMap.getOrDefault(inv.getBookId(), 0);
            double avgDailySales = salesVolume / 30.0;

            int currentStock = inv.getStockQuantity() != null ? inv.getStockQuantity() : 0;
            int minStock = inv.getMinStock() != null ? inv.getMinStock() : 10;
            int maxStock = inv.getMaxStock() != null ? inv.getMaxStock() : 1000;

            int daysOfStock = avgDailySales > 0 ? (int) (currentStock / avgDailySales) : 999;

            int suggestQuantity = 0;
            String suggestionLevel = "无需补货";

            if (currentStock <= minStock) {
                suggestQuantity = maxStock - currentStock;
                suggestionLevel = "紧急补货";
            } else if (avgDailySales > 0 && daysOfStock < 15) {
                suggestQuantity = (int) (avgDailySales * 30) - currentStock;
                if (suggestQuantity > 0) {
                    suggestionLevel = "建议补货";
                } else {
                    suggestQuantity = 0;
                    suggestionLevel = "库存充足";
                }
            } else if (avgDailySales == 0 && currentStock > maxStock * 0.8) {
                suggestionLevel = "库存积压";
            } else {
                suggestionLevel = "库存正常";
            }

            Map<String, Object> map = new HashMap<>();
            map.put("bookId", inv.getBookId());
            map.put("bookName", book.getBookName());
            map.put("author", book.getAuthor());
            map.put("currentStock", currentStock);
            map.put("minStock", minStock);
            map.put("maxStock", maxStock);
            map.put("monthlySales", salesVolume);
            map.put("avgDailySales", String.format("%.2f", avgDailySales));
            map.put("daysOfStock", daysOfStock);
            map.put("suggestQuantity", suggestQuantity);
            map.put("suggestionLevel", suggestionLevel);

            if (book.getSellingPrice() != null) {
                map.put("estimatedCost", book.getSellingPrice().multiply(BigDecimal.valueOf(suggestQuantity * 0.6)));
            }

            suggestions.add(map);
        }

        suggestions.sort((a, b) -> {
            String levelA = (String) a.get("suggestionLevel");
            String levelB = (String) b.get("suggestionLevel");
            Map<String, Integer> priority = new HashMap<>();
            priority.put("紧急补货", 1);
            priority.put("建议补货", 2);
            priority.put("库存正常", 3);
            priority.put("库存积压", 4);
            priority.put("无需补货", 5);
            return priority.getOrDefault(levelA, 5).compareTo(priority.getOrDefault(levelB, 5));
        });

        return Result.success(suggestions);
    }

    @PutMapping("/update")
    public Result<Void> updateInventory(@RequestBody Inventory inventory) {
        Inventory existing = inventoryService.getById(inventory.getInventoryId());
        if (existing != null) {
            if (inventory.getMinStock() != null) {
                existing.setMinStock(inventory.getMinStock());
            }
            if (inventory.getMaxStock() != null) {
                existing.setMaxStock(inventory.getMaxStock());
            }
            if (inventory.getStockQuantity() != null) {
                int diff = inventory.getStockQuantity() - existing.getStockQuantity();
                if (diff > 0) {
                    inventoryService.increaseStock(existing.getBookId(), diff);
                } else if (diff < 0) {
                    inventoryService.decreaseStock(existing.getBookId(), Math.abs(diff));
                }
            }
            inventoryService.updateById(existing);
        }
        return Result.success("更新成功");
    }
}
