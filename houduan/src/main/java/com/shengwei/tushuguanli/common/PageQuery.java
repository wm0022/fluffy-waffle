package com.shengwei.tushuguanli.common;

import lombok.Data;

/**
 * 分页查询参数
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String sortBy;
    private String sortOrder = "desc";

    public Integer getOffset() {
        return (pageNum - 1) * pageSize;
    }

    public Integer getLimit() {
        return pageSize;
    }
}
