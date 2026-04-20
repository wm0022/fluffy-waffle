package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 会员信息实体
 */
@Data
@TableName("member_info")
public class MemberInfo {

    @TableId(type = IdType.AUTO)
    private Long memberId;

    private String memberNo;

    private Long userId;

    private String idCard;

    private String realName;

    private Integer gender;

    private String nation;

    private String nativePlace;

    private LocalDate birthDate;

    private Integer age;

    private String education;

    private String address;

    private String phone;

    private String email;

    private String avatar;

    private Integer memberLevel;

    private Integer memberScore;

    private BigDecimal totalConsumption;

    private Integer totalOrders;

    private LocalDateTime lastOrderTime;

    private String preferences;

    private LocalDate birthday;

    private Integer status;

    private LocalDateTime registerTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
