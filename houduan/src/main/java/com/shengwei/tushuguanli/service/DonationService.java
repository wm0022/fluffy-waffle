package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.DonationRecord;

import java.util.List;
import java.util.Map;

/**
 * 捐赠服务
 */
public interface DonationService extends IService<DonationRecord> {

    /**
     * 提交捐赠申请
     */
    void submitDonation(DonationRecord record);

    /**
     * 获取用户捐赠记录
     */
    List<DonationRecord> getUserDonations(Long userId);

    /**
     * 获取所有捐赠记录（管理员）
     */
    List<DonationRecord> getAllDonations(Integer status);

    /**
     * 审核捐赠记录
     */
    void reviewDonation(Long id, Integer status, String reviewRemark, Long reviewerId);

    /**
     * 更新捐赠记录
     */
    void updateDonation(DonationRecord record);

    /**
     * 统计捐赠信息
     */
    Map<String, Object> countDonations();
}
