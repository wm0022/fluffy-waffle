package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.DonationPerson;

import java.util.List;
import java.util.Map;

/**
 * 捐赠人士服务
 */
public interface DonationPersonService extends IService<DonationPerson> {

    /**
     * 获取捐赠人士列表
     */
    List<DonationPerson> getDonationPersonList(Integer status);

    /**
     * 添加或更新捐赠人士
     */
    void saveOrUpdatePerson(DonationPerson person);

    /**
     * 统计捐赠人士信息
     */
    Map<String, Object> countPersons();
}
