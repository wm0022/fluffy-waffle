package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("book_info")
public class BookInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String bookName;

    private String author;

    private String editor;

    private String publisher;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate publishDate;

    private String edition;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate printDate;

    private String isbn;

    private Long categoryId;

    private BigDecimal originalPrice;

    private BigDecimal sellingPrice;

    private BigDecimal discount;

    private Integer stockCount;

    private Integer salesVolume;

    private Integer shelfStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime shelfTime;

    private String coverImage;

    private String description;

    private Integer isDonation;

    private Integer isHot;

    private Integer isNew;

    private LocalDateTime createTime;
}
