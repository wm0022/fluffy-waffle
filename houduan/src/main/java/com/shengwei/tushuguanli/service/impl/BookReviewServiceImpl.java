package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.BookReview;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.mapper.BookReviewMapper;
import com.shengwei.tushuguanli.service.BookReviewService;
import com.shengwei.tushuguanli.service.BookInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 图书评价服务实现类
 */
@Service
public class BookReviewServiceImpl extends ServiceImpl<BookReviewMapper, BookReview> implements BookReviewService {

    @Autowired
    private BookInfoService bookInfoService;

    @Override
    public Page<BookReview> pageReviewList(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<BookReview> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookReview> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            String customerName = (String) params.get("customerName");
            Long bookId = (Long) params.get("bookId");
            Integer rating = (Integer) params.get("rating");
            Integer auditStatus = (Integer) params.get("auditStatus");
            String startDate = (String) params.get("startDate");
            String endDate = (String) params.get("endDate");

            wrapper.like(StringUtils.hasText(customerName), BookReview::getCustomerName, customerName)
                    .eq(bookId != null, BookReview::getBookId, bookId)
                    .eq(rating != null, BookReview::getRating, rating)
                    .eq(auditStatus != null, BookReview::getAuditStatus, auditStatus);

            // 时间范围查询
            if (StringUtils.hasText(startDate)) {
                wrapper.ge(BookReview::getCreateTime, startDate + " 00:00:00");
            }
            if (StringUtils.hasText(endDate)) {
                wrapper.le(BookReview::getCreateTime, endDate + " 23:59:59");
            }
        }

        wrapper.orderByDesc(BookReview::getCreateTime);
        return page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReview(BookReview bookReview) {
        if (bookReview.getBookId() == null) {
            throw new BusinessException("图书ID不能为空");
        }
        if (bookReview.getRating() == null) {
            throw new BusinessException("评分不能为空");
        }
        if (StringUtils.isEmpty(bookReview.getContent())) {
            throw new BusinessException("评价内容不能为空");
        }

        if (bookReview.getOrderItemId() != null) {
            long existCount = count(new LambdaQueryWrapper<BookReview>()
                    .eq(BookReview::getOrderItemId, bookReview.getOrderItemId()));
            if (existCount > 0) {
                throw new BusinessException("该订单项已评价，不能重复评价");
            }
        }

        BookInfo bookInfo = bookInfoService.getById(bookReview.getBookId());
        if (bookInfo == null && StringUtils.hasText(bookReview.getBookName())) {
            LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(BookInfo::getBookName, bookReview.getBookName());
            bookInfo = bookInfoService.getOne(wrapper, false);
        }
        if (bookInfo != null) {
            bookReview.setBookId(bookInfo.getId());
        }

        if (bookReview.getAuditStatus() == null) {
            bookReview.setAuditStatus(1);
        }
        if (bookReview.getStatus() == null) {
            bookReview.setStatus(1);
        }
        if (bookReview.getHelpfulCount() == null) {
            bookReview.setHelpfulCount(0);
        }
        if (bookReview.getIsAnonymous() == null) {
            bookReview.setIsAnonymous(0);
        }

        save(bookReview);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReview(Long reviewId, Integer auditStatus, String auditRemark, Long auditUserId) {
        BookReview review = getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        // 只有待审核状态的评价可以审核
        if (review.getAuditStatus() != 1) {
            throw new BusinessException("该评价已审核，不能重复审核");
        }

        review.setAuditStatus(auditStatus);
        review.setAuditRemark(auditRemark);
        review.setAuditUserId(auditUserId);
        review.setAuditTime(LocalDateTime.now());

        // 如果审核不通过，可以设置状态为无效
        if (auditStatus == 3) { // 审核拒绝
            review.setStatus(0); // 无效
        }

        updateById(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void replyReview(Long reviewId, String replyContent, Long replyUserId) {
        BookReview review = getById(reviewId);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }

        // 只有审核通过的评价可以回复
        if (review.getAuditStatus() != 2) {
            throw new BusinessException("只有审核通过的评价可以回复");
        }

        review.setReplyContent(replyContent);
        review.setReplyUserId(replyUserId);
        review.setReplyTime(LocalDateTime.now());
        updateById(review);
    }

    @Override
    public Map<String, Object> countReviewStats(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = count();
        result.put("totalCount", totalCount);

        // 审核状态统计
        long pendingCount = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getAuditStatus, 1));
        result.put("pendingCount", pendingCount);

        long approvedCount = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getAuditStatus, 2));
        result.put("approvedCount", approvedCount);

        long rejectedCount = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getAuditStatus, 3));
        result.put("rejectedCount", rejectedCount);

        // 评分统计
        long rating1Count = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getRating, 1));
        result.put("rating1Count", rating1Count);

        long rating2Count = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getRating, 2));
        result.put("rating2Count", rating2Count);

        long rating3Count = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getRating, 3));
        result.put("rating3Count", rating3Count);

        long rating4Count = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getRating, 4));
        result.put("rating4Count", rating4Count);

        long rating5Count = count(new LambdaQueryWrapper<BookReview>()
                .eq(BookReview::getRating, 5));
        result.put("rating5Count", rating5Count);

        // 平均评分
        if (totalCount > 0) {
            Double avgRating = getBaseMapper().selectAvgRating();
            result.put("avgRating", avgRating != null ? avgRating : 0);
        } else {
            result.put("avgRating", 0);
        }

        return result;
    }
}