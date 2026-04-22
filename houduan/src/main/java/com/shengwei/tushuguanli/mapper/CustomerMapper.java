package com.shengwei.tushuguanli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengwei.tushuguanli.entity.Customer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 顾客 Mapper
 */
@Mapper
public interface CustomerMapper extends BaseMapper<Customer> {
}
