package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐赠记录实体
 */
@Data
@TableName("donation_record")
public class DonationRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String bookName;

    private String publisher;

    private Integer quantity;

    private String author;

    private String isbn;

    private String edition;

    private String binding;

    private String language;

    private BigDecimal price;

    private String description;

    private String coverImage;

    private Integer status;

    private String reviewRemark;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String userName;
}
