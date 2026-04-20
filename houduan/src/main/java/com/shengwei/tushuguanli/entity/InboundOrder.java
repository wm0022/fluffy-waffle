package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入库单实体
 */
@Data
@TableName("inbound_order")
public class InboundOrder {

    @TableId(type = IdType.AUTO)
    private Long inboundId;

    private String inboundNo;

    private Long bookId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalAmount;

    private Integer inboundType;

    private Integer sourceType;

    private Long sourceId;

    private String warehouseCode;

    private Long operatorId;

    private String operatorName;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime completeTime;
}
