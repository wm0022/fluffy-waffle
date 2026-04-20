package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务反馈实体
 */
@Data
@TableName("service_feedback")
public class ServiceFeedback {

    @TableId(type = IdType.AUTO)
    private Long feedbackId;

    private String feedbackNo;

    private Long memberId;

    private String customerName;

    private String customerPhone;

    private Integer feedbackType;

    private String feedbackModule;

    private String content;

    private String contactWay;

    private Integer auditStatus;

    private Long handlerId;

    private String handlerName;

    private String handleResult;

    private LocalDateTime handleTime;

    private Integer satisfaction;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}