package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.DonationAccept;

import java.util.Map;

/**
 * 捐赠验收服务接口
 */
public interface DonationAcceptService extends IService<DonationAccept> {

    /**
     * 分页查询捐赠验收列表
     */
    Page<DonationAccept> pageAcceptList(Integer pageNum, Integer pageSize, Map<String, Object> params);

    /**
     * 创建捐赠验收单
     */
    void createAccept(DonationAccept donationAccept);

    /**
     * 更新捐赠验收单
     */
    void updateAccept(DonationAccept donationAccept);

    /**
     * 完成验收
     */
    void completeAccept(Long acceptId);

    /**
     * 发放积分
     */
    void issueScore(Long acceptId);

    /**
     * 统计捐赠验收信息
     */
    Map<String, Object> countAcceptStats();
}