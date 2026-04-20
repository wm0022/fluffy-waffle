package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐赠验收实体
 */
@Data
@TableName("donation_accept")
public class DonationAccept {

    @TableId(type = IdType.AUTO)
    private Long acceptId;

    private String acceptNo;

    private Long applyId;

    private Long donorId;

    private String donorName;

    private String donorPhone;

    private Integer acceptType;

    private Integer bookCount;

    private BigDecimal totalValue;

    private Integer totalScore;

    private Integer scoreIssued;

    private Integer acceptResult;

    private String rejectReason;

    private Long acceptorId;

    private String acceptorName;

    private LocalDateTime acceptTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime completeTime;
}