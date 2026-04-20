package com.shengwei.tushuguanli.service;

/**
 * 会员服务
 */
public interface MemberService {

    /**
     * 根据累计消费金额计算会员等级
     * @param totalAmount 累计消费金额
     * @return 会员等级：0-非会员 1-普通会员 2-银卡会员 3-金卡会员 4-钻石卡会员
     */
    int calculateMemberLevel(java.math.BigDecimal totalAmount);

    /**
     * 根据会员等级获取折扣
     * @param memberLevel 会员等级
     * @return 折扣（如0.95表示95折）
     */
    double getDiscount(int memberLevel);

    /**
     * 根据会员等级获取等级名称
     * @param memberLevel 会员等级
     * @return 等级名称
     */
    String getLevelName(int memberLevel);

    /**
     * 计算积分（1元=1积分）
     * @param amount 消费金额
     * @return 积分
     */
    int calculatePoints(java.math.BigDecimal amount);

    /**
     * 更新用户会员信息（支付后调用）
     * @param userId 用户ID
     * @param payAmount 本次支付金额
     */
    void updateMemberInfo(Long userId, java.math.BigDecimal payAmount);

    /**
     * 获取用户会员信息
     * @param userId 用户ID
     * @return 会员信息
     */
    java.util.Map<String, Object> getMemberInfo(Long userId);
}
