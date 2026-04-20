package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 捐赠验收明细实体
 */
@Data
@TableName("donation_accept_item")
public class DonationAcceptItem {

    @TableId(type = IdType.AUTO)
    private Long itemId;

    private Long acceptId;

    private String isbn;

    private String bookName;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    private BigDecimal price;

    private Integer quantity;

    private Integer bookCondition;

    private String conditionRemark;

    private BigDecimal assessedValue;

    private Integer score;

    private Integer handleType;

    private String handleRemark;

    private Long bookId;

    private Integer status;

    private LocalDateTime createTime;
}