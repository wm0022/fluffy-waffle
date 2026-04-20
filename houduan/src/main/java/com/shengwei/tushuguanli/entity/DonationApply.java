package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐赠申请实体
 */
@Data
@TableName("donation_apply")
public class DonationApply {

    @TableId(type = IdType.AUTO)
    private Long applyId;

    private String applyNo;

    private Long donorId;

    private String donorName;

    private String donorPhone;

    private Integer donationType;

    private LocalDateTime appointTime;

    private Integer bookCount;

    private BigDecimal estimatedValue;

    private String remark;

    private Integer auditStatus;

    private String auditRemark;

    private Long auditUserId;

    private LocalDateTime auditTime;

    private Integer status;

    private LocalDateTime createTime;
}