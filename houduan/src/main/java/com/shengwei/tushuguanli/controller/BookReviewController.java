package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.PageResult;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.BookReview;
import com.shengwei.tushuguanli.entity.TradeOrderItem;
import com.shengwei.tushuguanli.service.BookInfoService;
import com.shengwei.tushuguanli.service.BookReviewService;
import com.shengwei.tushuguanli.mapper.TradeOrderItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/book-review")
public class BookReviewController {

    @Autowired
    private BookReviewService bookReviewService;

    @Autowired
    private BookInfoService bookInfoService;

    @Autowired
    private TradeOrderItemMapper orderItemMapper;

    @GetMapping("/page")
    public Result<PageResult<BookReview>> pageReviewList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String customerName,
            Long bookId,
            Integer rating,
            Integer auditStatus,
            String startDate,
            String endDate) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("customerName", customerName);
        params.put("bookId", bookId);
        params.put("rating", rating);
        params.put("auditStatus", auditStatus);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Page<BookReview> page = bookReviewService.pageReviewList(pageNum, pageSize, params);
        PageResult<BookReview> result = PageResult.of(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );

        return Result.success(result);
    }

    @GetMapping("/{reviewId}")
    public Result<BookReview> getReviewById(@PathVariable Long reviewId) {
        BookReview review = bookReviewService.getById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }
        return Result.success(review);
    }

    @GetMapping("/book/{bookId}")
    public Result<List<BookReview>> getReviewsByBookId(@PathVariable Long bookId) {
        List<BookReview> reviews = bookReviewService.list(
                new LambdaQueryWrapper<BookReview>()
                        .eq(BookReview::getBookId, bookId)
                        .eq(BookReview::getAuditStatus, 2)
                        .eq(BookReview::getStatus, 1)
                        .orderByDesc(BookReview::getCreateTime));
        return Result.success(reviews);
    }

    @PostMapping
    public Result<Void> addReview(@RequestBody BookReview bookReview) {
        bookReviewService.addReview(bookReview);
        return Result.success("评价添加成功");
    }

    @PostMapping("/{reviewId}/audit")
    public Result<Void> auditReview(@PathVariable Long reviewId, @RequestBody Map<String, Object> params) {
        Integer auditStatus = (Integer) params.get("auditStatus");
        String auditRemark = (String) params.get("auditRemark");
        Long auditUserId = Long.valueOf(params.get("auditUserId").toString());
        bookReviewService.auditReview(reviewId, auditStatus, auditRemark, auditUserId);
        return Result.success("审核操作成功");
    }

    @PostMapping("/{reviewId}/reply")
    public Result<Void> replyReview(@PathVariable Long reviewId, @RequestBody Map<String, Object> params) {
        String replyContent = (String) params.get("replyContent");
        Long replyUserId = Long.valueOf(params.get("replyUserId").toString());
        bookReviewService.replyReview(reviewId, replyContent, replyUserId);
        return Result.success("回复成功");
    }

    @PutMapping("/{reviewId}/toggle-visibility")
    public Result<Void> toggleVisibility(@PathVariable Long reviewId) {
        BookReview review = bookReviewService.getById(reviewId);
        if (review == null) {
            return Result.error("评价不存在");
        }
        review.setStatus(review.getStatus() == 1 ? 0 : 1);
        bookReviewService.updateById(review);
        return Result.success("操作成功");
    }

    @GetMapping("/count")
    public Result<Map<String, Object>> countReviewStats(
            Long bookId,
            String startDate,
            String endDate) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("bookId", bookId);
        params.put("startDate", startDate);
        params.put("endDate", endDate);

        Map<String, Object> result = bookReviewService.countReviewStats(params);
        return Result.success(result);
    }

    @GetMapping("/check")
    public Result<Boolean> checkReviewExists(@RequestParam Long orderItemId) {
        long count = bookReviewService.count(
                new LambdaQueryWrapper<BookReview>()
                        .eq(BookReview::getOrderItemId, orderItemId));
        return Result.success(count > 0);
    }

    @GetMapping("/fix-book-id")
    public Result<Integer> fixReviewBookId() {
        List<BookReview> reviews = bookReviewService.list();
        int fixedCount = 0;
        for (BookReview review : reviews) {
            BookInfo bookInfo = bookInfoService.getById(review.getBookId());
            if (bookInfo == null && review.getOrderItemId() != null) {
                TradeOrderItem orderItem = orderItemMapper.selectById(review.getOrderItemId());
                if (orderItem != null) {
                    List<BookInfo> books = bookInfoService.list(
                            new LambdaQueryWrapper<BookInfo>()
                                    .eq(BookInfo::getBookName, orderItem.getBookName()));
                    if (!books.isEmpty()) {
                        review.setBookId(books.get(0).getId());
                        bookReviewService.updateById(review);
                        fixedCount++;
                    }
                }
            }
        }
        return Result.success(fixedCount);
    }
}