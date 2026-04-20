package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.DonationApply;

import java.util.Map;

/**
 * 捐赠申请服务接口
 */
public interface DonationApplyService extends IService<DonationApply> {

    /**
     * 分页查询捐赠申请列表
     */
    Page<DonationApply> pageApplyList(Integer pageNum, Integer pageSize, Map<String, Object> params);

    /**
     * 创建捐赠申请
     */
    void createApply(DonationApply donationApply);

    /**
     * 更新捐赠申请
     */
    void updateApply(DonationApply donationApply);

    /**
     * 审核捐赠申请
     */
    void auditApply(Long applyId, Integer auditStatus, String auditRemark, Long auditUserId);

    /**
     * 完成捐赠申请
     */
    void completeApply(Long applyId);

    /**
     * 取消捐赠申请
     */
    void cancelApply(Long applyId);

    /**
     * 统计捐赠申请信息
     */
    Map<String, Object> countApplyStats();
}
