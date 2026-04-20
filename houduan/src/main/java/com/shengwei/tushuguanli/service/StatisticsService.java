package com.shengwei.tushuguanli.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 数据统计服务接口
 */
public interface StatisticsService {

    /**
     * 销售统计
     */
    Map<String, Object> salesStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 订单统计
     */
    Map<String, Object> orderStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 会员统计
     */
    Map<String, Object> memberStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 库存统计
     */
    Map<String, Object> inventoryStatistics();

    /**
     * 捐赠统计
     */
    Map<String, Object> donationStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * 图书销售排行
     */
    List<Map<String, Object>> bookSalesRanking(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 捐赠人排行
     */
    List<Map<String, Object>> donorRanking(LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * 月度销售趋势
     */
    List<Map<String, Object>> monthlySalesTrend(Integer year);

    /**
     * 会员增长趋势
     */
    List<Map<String, Object>> memberGrowthTrend(Integer year);
}