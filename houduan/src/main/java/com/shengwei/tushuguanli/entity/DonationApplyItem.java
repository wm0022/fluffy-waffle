package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 捐赠申请明细实体
 */
@Data
@TableName("donation_apply_item")
public class DonationApplyItem {

    @TableId(type = IdType.AUTO)
    private Long itemId;

    private Long applyId;

    private String isbn;

    private String bookName;

    private String author;

    private String publisher;

    private LocalDate publishDate;

    private Integer quantity;

    private Integer bookCondition;

    private BigDecimal estimatedPrice;

    private String remark;

    private LocalDateTime createTime;
}