package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 顾客实体类
 * 对应数据库表 customer（从 sys_user 拆分的顾客专用表）
 */
@Data
@TableName("customer")
public class Customer {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    @TableField("real_name")
    private String realName;

    private Integer gender;

    @TableField("id_card")
    private String idCard;

    private String nation;

    @TableField("native_place")
    private String nativePlace;

    @TableField("birth_date")
    private LocalDate birthDate;

    private Integer age;

    private String education;

    private String address;

    private String preferences;

    private String email;

    private String phone;

    private Integer status;

    @TableField("member_level")
    private Integer memberLevel;

    private Integer points;

    @TableField("total_amount")
    private java.math.BigDecimal totalAmount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
