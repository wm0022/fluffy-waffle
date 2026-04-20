package com.shengwei.tushuguanli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengwei.tushuguanli.entity.BookCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 图书分类 Mapper
 */
@Mapper
public interface BookCategoryMapper extends BaseMapper<BookCategory> {

    /**
     * 获取分类树
     */
    List<BookCategory> selectCategoryTree();
}
