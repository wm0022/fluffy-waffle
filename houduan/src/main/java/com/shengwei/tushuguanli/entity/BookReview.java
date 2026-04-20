package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("book_review")
public class BookReview {

    @TableId(type = IdType.AUTO)
    private Long reviewId;

    private Long orderItemId;

    private Long bookId;

    private Long memberId;

    private String customerName;

    private Integer rating;

    private String content;

    private String images;

    private Integer isAnonymous;

    private Integer storeRating;

    private Integer businessRating;

    private Integer serviceRating;

    private Integer auditStatus;

    private String auditRemark;

    private Long auditUserId;

    private LocalDateTime auditTime;

    private String replyContent;

    private Long replyUserId;

    private LocalDateTime replyTime;

    private Integer helpfulCount;

    private Integer status;

    private LocalDateTime createTime;

    @TableField(exist = false)
    private String bookName;
}
