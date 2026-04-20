package com.shengwei.tushuguanli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengwei.tushuguanli.entity.TradeOrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单退款Mapper
 */
@Mapper
public interface TradeOrderRefundMapper extends BaseMapper<TradeOrderRefund> {

    @Select("SELECT * FROM trade_order_refund WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<TradeOrderRefund> selectByUserId(Long userId);

    @Select("SELECT * FROM trade_order_refund WHERE order_no = #{orderNo} ORDER BY create_time DESC")
    List<TradeOrderRefund> selectByOrderNo(String orderNo);

    @Select("SELECT * FROM trade_order_refund WHERE status = #{status} ORDER BY create_time DESC")
    List<TradeOrderRefund> selectAllWithStatus(@Param("status") Integer status);
}
