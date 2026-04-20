package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 出库单实体
 */
@Data
@TableName("outbound_order")
public class OutboundOrder {

    @TableId(type = IdType.AUTO)
    private Long outboundId;

    private String outboundNo;

    private Long bookId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private Integer outboundType;

    private Integer targetType;

    private Long targetId;

    private String orderNo;

    private String warehouseCode;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime completeTime;
}
