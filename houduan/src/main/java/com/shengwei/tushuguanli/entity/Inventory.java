package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 库存实体
 */
@Data
@TableName("inventory")
public class Inventory {

    @TableId(type = IdType.AUTO)
    private Long inventoryId;

    private Long bookId;

    private Integer stockQuantity;

    private Integer availableQuantity;

    private Integer lockedQuantity;

    private String warehouseCode;

    private String warehouseName;

    private String location;

    private Integer minStock;

    private Integer maxStock;

    private Integer stockStatus;

    private LocalDateTime lastCheckTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
