package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.DonationPerson;
import com.shengwei.tushuguanli.entity.DonationRecord;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.mapper.DonationPersonMapper;
import com.shengwei.tushuguanli.mapper.DonationRecordMapper;
import com.shengwei.tushuguanli.service.DonationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DonationServiceImpl extends ServiceImpl<DonationRecordMapper, DonationRecord> implements DonationService {

    @Autowired
    private DonationPersonMapper donationPersonMapper;

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Override
    public void submitDonation(DonationRecord record) {
        record.setStatus(0);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        save(record);
    }

    @Override
    public List<DonationRecord> getUserDonations(Long userId) {
        return list(new LambdaQueryWrapper<DonationRecord>()
                .eq(DonationRecord::getUserId, userId)
                .orderByDesc(DonationRecord::getCreateTime));
    }

    @Override
    public List<DonationRecord> getAllDonations(Integer status) {
        LambdaQueryWrapper<DonationRecord> wrapper = new LambdaQueryWrapper<>();
        if (status != null && status >= 0) {
            wrapper.eq(DonationRecord::getStatus, status);
        }
        wrapper.orderByDesc(DonationRecord::getCreateTime);
        return list(wrapper);
    }

    @Override
    public void reviewDonation(Long id, Integer status, String reviewRemark, Long reviewerId) {
        DonationRecord record = getById(id);
        if (record == null) {
            throw new RuntimeException("捐赠记录不存在");
        }
        if (record.getStatus() != 0) {
            throw new RuntimeException("该记录已审核");
        }

        record.setStatus(status);
        record.setReviewRemark(reviewRemark);
        record.setReviewerId(reviewerId);
        record.setReviewTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);

        if (status == 1) {
            // 审核通过后自动创建图书信息(isDonation=1, shelfStatus=0 需手动上架)
            BookInfo bookInfo = new BookInfo();
            bookInfo.setBookName(record.getBookName());
            bookInfo.setAuthor(record.getAuthor());
            bookInfo.setPublisher(record.getPublisher());
            bookInfo.setIsbn(record.getIsbn());
            bookInfo.setEdition(record.getEdition());
            bookInfo.setSellingPrice(record.getPrice() != null ? record.getPrice() : java.math.BigDecimal.ZERO);
            bookInfo.setOriginalPrice(record.getPrice());
            bookInfo.setStockCount(record.getQuantity() != null ? record.getQuantity() : 1);
            bookInfo.setSalesVolume(0);
            bookInfo.setShelfStatus(0);
            bookInfo.setIsDonation(1);
            bookInfo.setDescription(record.getDescription());
            bookInfo.setCreateTime(LocalDateTime.now());
            bookInfoMapper.insert(bookInfo);

            // 审核通过后自动添加到爱心赠书人士列表
            LambdaQueryWrapper<DonationPerson> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DonationPerson::getUserId, record.getUserId());
            DonationPerson existing = donationPersonMapper.selectOne(wrapper);

            if (existing == null) {
                DonationPerson person = new DonationPerson();
                person.setUserId(record.getUserId());
                person.setDonationCount(record.getQuantity() != null ? record.getQuantity() : 1);
                person.setDonationAmount(record.getPrice() != null ? record.getPrice() : java.math.BigDecimal.ZERO);
                person.setPoints(record.getQuantity() != null ? record.getQuantity() * 10 : 10);
                person.setStatus(1);
                person.setCreateTime(LocalDateTime.now());
                person.setUpdateTime(LocalDateTime.now());
                donationPersonMapper.insert(person);
            } else {
                existing.setDonationCount(existing.getDonationCount() + (record.getQuantity() != null ? record.getQuantity() : 1));
                if (record.getPrice() != null) {
                    existing.setDonationAmount(existing.getDonationAmount().add(record.getPrice()));
                }
                existing.setPoints(existing.getPoints() + (record.getQuantity() != null ? record.getQuantity() * 10 : 10));
                existing.setUpdateTime(LocalDateTime.now());
                donationPersonMapper.updateById(existing);
            }
        }
    }

    @Override
    public void updateDonation(DonationRecord record) {
        record.setUpdateTime(LocalDateTime.now());
        updateById(record);
    }

    @Override
    public Map<String, Object> countDonations() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", count());
        result.put("pending", count(new LambdaQueryWrapper<DonationRecord>().eq(DonationRecord::getStatus, 0)));
        result.put("approved", count(new LambdaQueryWrapper<DonationRecord>().eq(DonationRecord::getStatus, 1)));
        result.put("rejected", count(new LambdaQueryWrapper<DonationRecord>().eq(DonationRecord::getStatus, 2)));
        return result;
    }
}
