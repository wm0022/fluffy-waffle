package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 爱心赠书人士实体
 */
@Data
@TableName("donation_person")
public class DonationPerson {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String idCard;

    private String realName;

    private Integer gender;

    private String ethnicity;

    private String hometown;

    private LocalDate birthDate;

    private String education;

    private String address;

    private String phone;

    private Integer donationCount;

    private BigDecimal donationAmount;

    private Integer points;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
