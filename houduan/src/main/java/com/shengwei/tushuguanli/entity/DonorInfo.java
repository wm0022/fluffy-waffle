package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 捐赠人士实体
 */
@Data
@TableName("donor_info")
public class DonorInfo {

    @TableId(type = IdType.AUTO)
    private Long donorId;

    private String donorNo;

    private String idCard;

    private String realName;

    private Integer gender;

    private String nation;

    private String nativePlace;

    private java.time.LocalDate birthDate;

    private Integer age;

    private String education;

    private String address;

    private String phone;

    private String email;

    private Integer totalBooks;

    private BigDecimal totalAmount;

    private Integer totalScore;

    private String remark;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
