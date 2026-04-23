package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.DonorInfo;
import com.shengwei.tushuguanli.entity.DonationRecord;
import com.shengwei.tushuguanli.entity.Customer;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.mapper.DonorInfoMapper;
import com.shengwei.tushuguanli.mapper.DonationRecordMapper;
import com.shengwei.tushuguanli.mapper.CustomerMapper;
import com.shengwei.tushuguanli.service.DonationService;
import com.shengwei.tushuguanli.service.InventoryService;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DonationServiceImpl extends ServiceImpl<DonationRecordMapper, DonationRecord> implements DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationServiceImpl.class);

    @Autowired
    private BookInfoMapper bookInfoMapper;

    @Autowired
    private DonorInfoMapper donorInfoMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private InventoryService inventoryService;

    @Override
    public void submitDonation(DonationRecord record) {
        // 从 SecurityContext 获取当前登录用户ID（优先），防止前端伪造 userId
        Long currentUserId = com.shengwei.tushuguanli.config.SecurityContext.getCurrentUserId();
        if (currentUserId != null) {
            record.setUserId(currentUserId);
        }
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
        List<DonationRecord> records = list(wrapper);

        // 批量查询关联用户姓名
        Set<Long> userIds = records.stream()
                .map(DonationRecord::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (!userIds.isEmpty()) {
            Map<Long, String> userMap = customerMapper.selectBatchIds(userIds).stream()
                    .collect(Collectors.toMap(Customer::getId,
                            u -> u.getRealName() != null ? u.getRealName() : u.getUsername(),
                            (a, b) -> a));
            for (DonationRecord record : records) {
                record.setUserName(userMap.getOrDefault(record.getUserId(), ""));
            }
        }
        return records;
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
            if (StringUtils.hasText(record.getCoverImage())) {
                bookInfo.setCoverImage(record.getCoverImage());
            }
            bookInfo.setCreateTime(LocalDateTime.now());
            bookInfoMapper.insert(bookInfo);

            // 同步创建库存记录（修复：公益图书下单时"库存不存在"的问题）
            Integer stockQuantity = record.getQuantity() != null ? record.getQuantity() : 1;
            try {
                inventoryService.createInventory(bookInfo.getId(), stockQuantity);
                log.info("[reviewDonation] 公益图书库存初始化成功, bookId={}, quantity={}", bookInfo.getId(), stockQuantity);
            } catch (Exception e) {
                log.error("[reviewDonation] 公益图书库存初始化失败, bookId={}, quantity={}: {}",
                        bookInfo.getId(), stockQuantity, e.getMessage(), e);
                throw new RuntimeException("公益图书库存初始化失败: " + e.getMessage(), e);
            }

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
     * 同时更新 customer 表的积分和消费金额，确保个人中心显示一致
     * 匹配优先级：userId（主键）> 手机号 > 姓名
     */
    private void syncDonorInfo(DonationRecord record) {
        Long userId = record.getUserId();
        int quantity = record.getQuantity() != null ? record.getQuantity() : 1;
        BigDecimal price = record.getPrice() != null ? record.getPrice() : BigDecimal.ZERO;
        int earnedPoints = quantity * 10;  // 每本捐赠图书获得10积分
        log.info("[syncDonorInfo] 开始同步, donationId={}, userId={}, quantity={}, earnedPoints={}",
                record.getId(), userId, quantity, earnedPoints);

        // 1) 查询 customer 表获取顾客信息
        if (userId == null) {
            log.warn("[syncDonorInfo] userId 为空, 跳过同步");
            return;
        }

        Customer user;
        try {
            user = customerMapper.selectById(userId);
        } catch (Exception e) {
            log.error("[syncDonorInfo] 查询 customer 失败, userId={}", userId, e);
            throw new RuntimeException("查询用户信息失败，无法同步捐赠者数据", e);
        }

        if (user == null) {
            log.error("[syncDonorInfo] customer 中不存在该用户, userId={}, 同步终止", userId);
            throw new RuntimeException("未找到用户信息（userId=" + userId + "），无法同步捐赠者数据，请检查用户是否已被删除");
        }

        // 2) 匹配 donor_info 记录（三级匹配：userId > 手机号 > 姓名）
        DonorInfo existingDonor = null;

        // 第一优先级：通过 userId 精确匹配
        LambdaQueryWrapper<DonorInfo> userIdWrapper = new LambdaQueryWrapper<>();
        userIdWrapper.eq(DonorInfo::getUserId, userId);
        existingDonor = donorInfoMapper.selectOne(userIdWrapper);

        if (existingDonor != null) {
            log.debug("[syncDonorInfo] 通过 userId={} 匹配到已有记录, donorId={}", userId, existingDonor.getDonorId());
        } else {
            // 第二优先级：通过手机号匹配
            if (StringUtils.hasText(user.getPhone())) {
                LambdaQueryWrapper<DonorInfo> phoneWrapper = new LambdaQueryWrapper<>();
                phoneWrapper.eq(DonorInfo::getPhone, user.getPhone());
                existingDonor = donorInfoMapper.selectOne(phoneWrapper);
                if (existingDonor != null) {
                    log.info("[syncDonorInfo] 通过手机号={} 匹配到已有记录(将补全userId), donorId={}",
                            user.getPhone(), existingDonor.getDonorId());
                    // 补全 userId 关联，防止下次再创建重复记录
                    existingDonor.setUserId(userId);
                    donorInfoMapper.updateById(existingDonor);
                }
            }

            // 第三优先级：通过姓名匹配（仅在前两轮都未命中时）
            if (existingDonor == null && StringUtils.hasText(user.getRealName())) {
                LambdaQueryWrapper<DonorInfo> nameWrapper = new LambdaQueryWrapper<>();
                nameWrapper.eq(DonorInfo::getRealName, user.getRealName());
                existingDonor = donorInfoMapper.selectOne(nameWrapper);
                if (existingDonor != null) {
                    log.info("[syncDonorInfo] 通过姓名={} 匹配到已有记录(将补全userId), donorId={}",
                            user.getRealName(), existingDonor.getDonorId());
                    existingDonor.setUserId(userId);
                    donorInfoMapper.updateById(existingDonor);
                }
            }
        }

        // 3) 新增或累加
        if (existingDonor == null) {
            // 首次捐赠 -> 新增 donor_info 记录
            log.info("[syncDonorInfo] 未匹配到已有记录, 创建新捐赠者, userId={}, phone={}", userId, user.getPhone());
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
            donorInfo.setPhone(user.getPhone() != null ? user.getPhone() : "");
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
                    log.info("[syncDonorInfo] 新增捐赠者成功, donorId={}, 总书籍={}, 总金额={}",
                            donorInfo.getDonorId(), quantity, price);
                } else {
                    throw new RuntimeException("新增捐赠者信息失败: insert 返回 0 行");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.error("[syncDonorInfo] 新增捐赠者异常, userId={}: {}", userId, e.getMessage(), e);
                throw new RuntimeException("同步捐赠者信息失败: " + e.getMessage(), e);
            }
        } else {
            // 非首次 -> 累加赠书数量、金额、积分，并同步最新个人信息
            int oldBooks = existingDonor.getTotalBooks() != null ? existingDonor.getTotalBooks() : 0;
            existingDonor.setTotalBooks(oldBooks + quantity);

            BigDecimal oldAmount = existingDonor.getTotalAmount() != null ? existingDonor.getTotalAmount() : BigDecimal.ZERO;
            existingDonor.setTotalAmount(oldAmount.add(price));

            int oldScore = existingDonor.getTotalScore() != null ? existingDonor.getTotalScore() : 0;
            existingDonor.setTotalScore(oldScore + (quantity * 10));

            // 同步 customer 表中的最新个人信息到 donor_info
            existingDonor.setRealName(user.getRealName() != null ? user.getRealName() : existingDonor.getRealName());
            existingDonor.setIdCard(user.getIdCard() != null ? user.getIdCard() : existingDonor.getIdCard());
            existingDonor.setGender(user.getGender() != null ? user.getGender() : existingDonor.getGender());
            existingDonor.setNation(user.getNation() != null ? user.getNation() : existingDonor.getNation());
            existingDonor.setNativePlace(user.getNativePlace() != null ? user.getNativePlace() : existingDonor.getNativePlace());
            existingDonor.setBirthDate(user.getBirthDate() != null ? user.getBirthDate() : existingDonor.getBirthDate());
            existingDonor.setAge(user.getAge() != null ? user.getAge() : existingDonor.getAge());
            existingDonor.setEducation(user.getEducation() != null ? user.getEducation() : existingDonor.getEducation());
            existingDonor.setAddress(user.getAddress() != null ? user.getAddress() : existingDonor.getAddress());
            existingDonor.setPhone(user.getPhone() != null ? user.getPhone() : existingDonor.getPhone());
            existingDonor.setEmail(user.getEmail() != null ? user.getEmail() : existingDonor.getEmail());

            existingDonor.setUpdateTime(LocalDateTime.now());

            try {
                int rows = donorInfoMapper.updateById(existingDonor);
                if (rows > 0) {
                    log.info("[syncDonorInfo] 更新捐赠者成功, donorId={}, 书籍 {}->{}, 金额 {}->{}, 积分 {}->{}",
                            existingDonor.getDonorId(),
                            oldBooks, existingDonor.getTotalBooks(),
                            oldAmount, existingDonor.getTotalAmount(),
                            oldScore, existingDonor.getTotalScore());
                } else {
                    throw new RuntimeException("更新捐赠者信息失败: updateById 返回 0 行");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                log.error("[syncDonorInfo] 更新捐赠者异常, donorId={}: {}", existingDonor.getDonorId(), e.getMessage(), e);
                throw new RuntimeException("更新捐赠者信息失败: " + e.getMessage(), e);
            }
        }

        // ========== 同步更新 customer 表的积分和消费金额（个人中心数据源） ==========
        try {
            Customer customer = customerMapper.selectById(userId);
            if (customer != null) {
                int currentPoints = customer.getPoints() != null ? customer.getPoints() : 0;
                BigDecimal currentTotal = customer.getTotalAmount() != null ? customer.getTotalAmount() : BigDecimal.ZERO;

                customer.setPoints(currentPoints + earnedPoints);
                customer.setTotalAmount(currentTotal.add(price));
                // 根据新的总消费金额重新计算会员等级
                int newLevel = new com.shengwei.tushuguanli.service.impl.MemberServiceImpl()
                        .calculateMemberLevel(customer.getTotalAmount());
                customer.setMemberLevel(newLevel);
                customer.setUpdateTime(LocalDateTime.now());

                customerMapper.updateById(customer);
                log.info("[syncDonorInfo] 顾客积分已同步, userId={}, 积分 {}->{}, 消费金额 {}->{}, 会员等级->{}",
                        userId, currentPoints, customer.getPoints(),
                        currentTotal, customer.getTotalAmount(), newLevel);
            }
        } catch (Exception e) {
            log.error("[syncDonorInfo] 同步顾客积分失败（不影响主流程）, userId={}: {}", userId, e.getMessage(), e);
            // 不抛异常，donor_info 已写入成功，积分同步失败可后续补偿
        }
    }
}
