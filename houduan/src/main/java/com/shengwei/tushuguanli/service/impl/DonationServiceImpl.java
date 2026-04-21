package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.DonorInfo;
import com.shengwei.tushuguanli.entity.DonationRecord;
import com.shengwei.tushuguanli.entity.SysUser;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.mapper.DonorInfoMapper;
import com.shengwei.tushuguanli.mapper.DonationRecordMapper;
import com.shengwei.tushuguanli.mapper.SysUserMapper;
import com.shengwei.tushuguanli.service.DonationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DonationServiceImpl extends ServiceImpl<DonationRecordMapper, DonationRecord> implements DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationServiceImpl.class);

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private DonorInfoMapper donorInfoMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

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
    @Transactional(rollbackFor = Exception.class)
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
            bookInfo.setSellingPrice(record.getPrice() != null ? record.getPrice() : BigDecimal.ZERO);
            bookInfo.setOriginalPrice(record.getPrice());
            bookInfo.setStockCount(record.getQuantity() != null ? record.getQuantity() : 1);
            bookInfo.setSalesVolume(0);
            bookInfo.setShelfStatus(0);
            bookInfo.setIsDonation(1);
            bookInfo.setDescription(record.getDescription());
            bookInfo.setCreateTime(LocalDateTime.now());
            bookInfoMapper.insert(bookInfo);

            // 同步写入 donor_info 表（唯一数据源）
            syncDonorInfo(record);
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

    /**
     * 同步捐赠人士信息到 donor_info 表（统一单表）
     * 根据 userId 查询 sys_user 获取个人信息，写入/更新 donor_info
     */
    private void syncDonorInfo(DonationRecord record) {
        Long userId = record.getUserId();
        log.info("[syncDonorInfo] 开始同步捐赠者信息, donationRecordId={}, userId={}", record.getId(), userId);

        // 1) 通过 userId 查询用户信息
        SysUser user = null;
        if (userId != null) {
            try {
                user = sysUserMapper.selectById(userId);
            } catch (Exception e) {
                log.error("[syncDonorInfo] 查询 sys_user 失败, userId={}", userId, e);
            }
        }

        if (user == null) {
            // 降级：尝试从 donation_record 中已有的信息构建（如果有扩展字段）
            log.warn("[syncDonorInfo] 未找到 sys_user 记录, userId={}, 将跳过同步", userId);
            return;
        }

        // 2) 构建查询条件：优先用手机号匹配，其次用 realName
        LambdaQueryWrapper<DonorInfo> donorWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(user.getPhone())) {
            donorWrapper.eq(DonorInfo::getPhone, user.getPhone());
            log.debug("[syncDonorInfo] 使用手机号匹配: {}", user.getPhone());
        } else if (StringUtils.hasText(user.getRealName())) {
            donorWrapper.eq(DonorInfo::getRealName, user.getRealName());
            log.debug("[syncDonorInfo] 使用姓名匹配: {}", user.getRealName());
        } else {
            log.warn("[syncDonorInfo] sys_user 缺少 phone 和 realName 字段, userId={}, 无法匹配, 跳过", userId);
            return;
        }

        DonorInfo existingDonor = donorInfoMapper.selectOne(donorWrapper);

        int quantity = record.getQuantity() != null ? record.getQuantity() : 1;
        BigDecimal price = record.getPrice() != null ? record.getPrice() : BigDecimal.ZERO;

        if (existingDonor == null) {
            // 首次捐赠 -> 新增 donor_info 记录
            DonorInfo donorInfo = new DonorInfo();
            donorInfo.setUserId(userId);
            donorInfo.setRealName(user.getRealName() != null ? user.getRealName() : "");
            donorInfo.setIdCard(user.getIdCard());
            donorInfo.setGender(user.getGender() != null ? user.getGender() : 1);
            donorInfo.setNation(user.getNation());
            donorInfo.setNativePlace(user.getNativePlace());
            donorInfo.setBirthDate(user.getBirthDate());
            donorInfo.setAge(user.getAge());
            donorInfo.setEducation(user.getEducation());
            donorInfo.setAddress(user.getAddress());
            donorInfo.setPhone(user.getPhone());
            donorInfo.setEmail(user.getEmail());
            donorInfo.setTotalBooks(quantity);
            donorInfo.setTotalAmount(price);
            donorInfo.setTotalScore(quantity * 10);
            donorInfo.setStatus(1);
            donorInfo.setCreateTime(LocalDateTime.now());
            donorInfo.setUpdateTime(LocalDateTime.now());

            try {
                int rows = donorInfoMapper.insert(donorInfo);
                if (rows > 0) {
                    log.info("[syncDonorInfo] 新增捐赠者成功, donorId={}, phone={}", donorInfo.getDonorId(), donorInfo.getPhone());
                } else {
                    log.error("[syncDonorInfo] 新增捐赠者失败, insert返回0行, phone={}", donorInfo.getPhone());
                    throw new RuntimeException("新增捐赠者信息失败: insert 返回 0 行");
                }
            } catch (RuntimeException e) {
                throw e; // 重新抛出，触发 @Transactional 回滚
            } catch (Exception e) {
                log.error("[syncDonorInfo] 新增捐赠者异常, phone={}", user.getPhone(), e);
                throw new RuntimeException("同步捐赠者信息到 donor_info 表失败: " + e.getMessage(), e);
            }
        } else {
            // 非首次 -> 累加赠书数量、金额、积分
            existingDonor.setTotalBooks(existingDonor.getTotalBooks() + quantity);
            existingDonor.setTotalAmount(existingDonor.getTotalAmount().add(price));
            existingDonor.setTotalScore(existingDonor.getTotalScore() + (quantity * 10));
            existingDonor.setUpdateTime(LocalDateTime.now());

            try {
                int rows = donorInfoMapper.updateById(existingDonor);
                if (rows > 0) {
                    log.info("[syncDonorInfo] 更新捐赠者成功, donorId={}, 累计书籍={}, 累计金额={}",
                            existingDonor.getDonorId(), existingDonor.getTotalBooks(), existingDonor.getTotalAmount());
                } else {
                    log.error("[syncDonorInfo] 更新捐赠者失败, updateById返回0行, donorId={}", existingDonor.getDonorId());
                    throw new RuntimeException("更新捐赠者信息失败: updateById 返回 0 行");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.error("[syncDonorInfo] 更新捐赠者异常, donorId={}", existingDonor.getDonorId(), e);
                throw new RuntimeException("更新捐赠者信息失败: " + e.getMessage(), e);
            }
        }
    }
}
