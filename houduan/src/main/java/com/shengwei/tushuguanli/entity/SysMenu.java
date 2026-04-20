package com.shengwei.tushuguanli.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单权限实体
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    @TableId(type = IdType.AUTO)
    private Long menuId;

    private Long parentId;

    private String menuName;

    private Integer menuType;

    private String path;

    private String component;

    private String perms;

    private String icon;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createTime;

    /**
     * 非数据库字段，用于构建树形菜单时挂载子菜单（@TableField(exist=false)）
     */
    @TableField(exist = false)
    private List<SysMenu> children;
}
