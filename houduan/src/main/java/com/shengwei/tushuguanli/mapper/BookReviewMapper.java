package com.shengwei.tushuguanli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengwei.tushuguanli.entity.BookReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 图书评价 Mapper
 */
@Mapper
public interface BookReviewMapper extends BaseMapper<BookReview> {

    /**
     * 计算平均评分
     */
    @Select("SELECT AVG(rating) FROM book_review WHERE audit_status = 2")
    Double selectAvgRating();
}