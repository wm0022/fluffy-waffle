package com.shengwei.tushuguanli.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车项 DTO（包含图书信息）
 */
@Data
public class CartItemVO {
    
    private Long id;
    
    private Long userId;
    
    private Long bookId;
    
    private Integer quantity;
    
    // 图书信息
    private String bookName;
    
    private String author;
    
    private BigDecimal sellingPrice;
    
    private String coverImage;
    
    // 小计金额
    private BigDecimal subtotal;
    
    // 库存数量
    private Integer stockCount;
}
