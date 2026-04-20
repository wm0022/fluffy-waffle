package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体
 */
@Data
@TableName("sys_operation_log")
public class SysOperationLog {

    @TableId(type = IdType.AUTO)
    private Long logId;

    private Long userId;

    private String username;

    private String operation;

    private String module;

    private String method;

    private String params;

    private String result;

    private String ipAddress;

    private Long executeTime;

    private Integer status;

    private String errorMsg;

    private LocalDateTime createTime;
}
