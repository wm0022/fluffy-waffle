package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.BookReview;

import java.util.Map;

/**
 * 图书评价服务接口
 */
public interface BookReviewService extends IService<BookReview> {

    /**
     * 分页查询图书评价列表
     */
    Page<BookReview> pageReviewList(Integer pageNum, Integer pageSize, Map<String, Object> params);

    /**
     * 添加评价
     */
    void addReview(BookReview bookReview);

    /**
     * 审核评价
     */
    void auditReview(Long reviewId, Integer auditStatus, String auditRemark, Long auditUserId);

    /**
     * 回复评价
     */
    void replyReview(Long reviewId, String replyContent, Long replyUserId);

    /**
     * 统计评价信息
     */
    Map<String, Object> countReviewStats(Map<String, Object> params);
}