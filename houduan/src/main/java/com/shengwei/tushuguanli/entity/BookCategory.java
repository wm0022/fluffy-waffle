package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图书分类实体
 */
@Data
@TableName("book_category")
public class BookCategory {

    @TableId(type = IdType.AUTO)
    private Long categoryId;

    private Long parentId;

    private String categoryName;

    private Integer level;

    private Integer sortOrder;

    private String icon;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
