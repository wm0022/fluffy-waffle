package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.Inventory;
import com.shengwei.tushuguanli.entity.TradeOrder;
import com.shengwei.tushuguanli.entity.TradeOrderItem;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.mapper.TradeOrderItemMapper;
import com.shengwei.tushuguanli.mapper.TradeOrderMapper;
import com.shengwei.tushuguanli.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页控制器
 */
@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private TradeOrderMapper orderMapper;

    @Autowired
    private TradeOrderItemMapper orderItemMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    /**
     * 获取待办事项统计
     */
    @GetMapping("/todo-stats")
    public Result<Map<String, Object>> getTodoStats() {
        Map<String, Object> result = new HashMap<>();

        // 库存预警数量
        List<Inventory> allInventories = inventoryService.list();
        long inventoryWarningCount = allInventories.stream()
                .filter(inv -> {
                    int stock = inv.getStockQuantity() != null ? inv.getStockQuantity() : 0;
                    int minStock = inv.getMinStock() != null ? inv.getMinStock() : 10;
                    return stock <= minStock;
                })
                .count();
        result.put("inventoryWarningCount", inventoryWarningCount);

        return Result.success(result);
    }

    /**
     * 获取销售统计数据（今日、本周、本月）
     */
    @GetMapping("/sales-stats")
    public Result<Map<String, Object>> getSalesStats() {
        Map<String, Object> result = new HashMap<>();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 今日销售
        Map<String, Object> todayStats = getSalesByPeriod(todayStart, now);
        result.put("today", todayStats);

        // 本周销售
        Map<String, Object> weekStats = getSalesByPeriod(weekStart, now);
        result.put("week", weekStats);

        // 本月销售
        Map<String, Object> monthStats = getSalesByPeriod(monthStart, now);
        result.put("month", monthStats);

        // 热销图书TOP5
        List<Map<String, Object>> topBooks = getTopSellingBooks(5);
        result.put("topBooks", topBooks);

        // 近7天销售趋势
        List<Map<String, Object>> salesTrend = getSalesTrend(7);
        result.put("salesTrend", salesTrend);

        return Result.success(result);
    }

    /**
     * 获取指定时间段的销售统计
     */
    private Map<String, Object> getSalesByPeriod(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> stats = new HashMap<>();

        // 查询已支付订单 (status = 1 表示已支付)
        LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(TradeOrder::getCreateTime, startTime)
                .le(TradeOrder::getCreateTime, endTime)
                .eq(TradeOrder::getStatus, 1);
        List<TradeOrder> orders = orderMapper.selectList(orderWrapper);

        int orderCount = orders.size();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalSales = 0;

        for (TradeOrder order : orders) {
            totalRevenue = totalRevenue.add(order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO);

            // 统计销量
            LambdaQueryWrapper<TradeOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(TradeOrderItem::getOrderId, order.getId());
            List<TradeOrderItem> items = orderItemMapper.selectList(itemWrapper);
            for (TradeOrderItem item : items) {
                totalSales += item.getQuantity() != null ? item.getQuantity() : 0;
            }
        }

        stats.put("orderCount", orderCount);
        stats.put("totalRevenue", totalRevenue);
        stats.put("totalSales", totalSales);

        return stats;
    }

    /**
     * 获取热销图书排行
     */
    private List<Map<String, Object>> getTopSellingBooks(int limit) {
        // 查询近30天已支付订单
        LocalDateTime startTime = LocalDate.now().minusDays(30).atStartOfDay();
        LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.ge(TradeOrder::getCreateTime, startTime)
                .eq(TradeOrder::getStatus, 1);
        List<TradeOrder> orders = orderMapper.selectList(orderWrapper);

        // 统计每本书的销量和销售额
        Map<Long, Integer> bookSalesMap = new HashMap<>();
        Map<Long, BigDecimal> bookRevenueMap = new HashMap<>();
        Map<Long, String> bookNameMap = new HashMap<>();

        for (TradeOrder order : orders) {
            LambdaQueryWrapper<TradeOrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(TradeOrderItem::getOrderId, order.getId());
            List<TradeOrderItem> items = orderItemMapper.selectList(itemWrapper);

            for (TradeOrderItem item : items) {
                bookSalesMap.merge(item.getBookId(), item.getQuantity() != null ? item.getQuantity() : 0, Integer::sum);
                BigDecimal revenue = (item.getPrice() != null ? item.getPrice() : BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(item.getQuantity() != null ? item.getQuantity() : 0));
                bookRevenueMap.merge(item.getBookId(), revenue, BigDecimal::add);
                bookNameMap.putIfAbsent(item.getBookId(), item.getBookName() != null ? item.getBookName() : "未知图书");
            }
        }

        // 从book_info表补充信息
        for (Long bookId : bookSalesMap.keySet()) {
            BookInfo book = bookInfoMapper.selectById(bookId);
            if (book != null) {
                bookNameMap.put(bookId, book.getBookName());
            }
        }

        // 按销量排序，取TOP N
        return bookSalesMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
                .limit(limit)
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
    }

    /**
     * 获取近N天销售趋势
     */
    private List<Map<String, Object>> getSalesTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();

            // 查询当天已支付订单
            LambdaQueryWrapper<TradeOrder> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.ge(TradeOrder::getCreateTime, dayStart)
                    .lt(TradeOrder::getCreateTime, dayEnd)
                    .eq(TradeOrder::getStatus, 1);
            List<TradeOrder> orders = orderMapper.selectList(orderWrapper);

            int daySales = 0;
            BigDecimal dayRevenue = BigDecimal.ZERO;

            for (TradeOrder order : orders) {
                dayRevenue = dayRevenue.add(order.getPayAmount() != null ? order.getPayAmount() : BigDecimal.ZERO);

                LambdaQueryWrapper<TradeOrderItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(TradeOrderItem::getOrderId, order.getId());
                List<TradeOrderItem> items = orderItemMapper.selectList(itemWrapper);
                for (TradeOrderItem item : items) {
                    daySales += item.getQuantity() != null ? item.getQuantity() : 0;
                }
            }

            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", date.toString());
            dayMap.put("salesVolume", daySales);
            dayMap.put("revenue", dayRevenue);
            trend.add(dayMap);
        }

        return trend;
    }
}
