package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.DonorInfo;

import java.util.Map;

/**
 * 捐赠人士服务接口
 */
public interface DonorInfoService extends IService<DonorInfo> {

    /**
     * 分页查询捐赠人士列表
     */
    Page<DonorInfo> pageDonorList(Integer pageNum, Integer pageSize, Map<String, Object> params);

    /**
     * 根据身份证号查询捐赠人士
     */
    DonorInfo getByIdCard(String idCard);

    /**
     * 根据电话查询捐赠人士
     */
    DonorInfo getByPhone(String phone);

    /**
     * 添加捐赠人士
     */
    void addDonor(DonorInfo donorInfo);

    /**
     * 更新捐赠人士
     */
    void updateDonor(DonorInfo donorInfo);

    /**
     * 删除捐赠人士
     */
    void deleteDonor(Long donorId);

    /**
     * 统计捐赠信息
     */
    Map<String, Object> countDonorStats();
}
