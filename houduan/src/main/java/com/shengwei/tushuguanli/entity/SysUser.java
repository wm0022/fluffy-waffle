package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class SysUser {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private Integer gender;

    private String idCard;

    private String nation;

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

    @TableField("user_type")
    private Integer userType;

    private Integer memberLevel;

    private Integer points;

    private java.math.BigDecimal totalAmount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
