package com.shengwei.tushuguanli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shengwei.tushuguanli.entity.BookInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 图书信息 Mapper
 */
@Mapper
public interface BookInfoMapper extends BaseMapper<BookInfo> {

    /**
     * 多条件查询图书列表
     */
    List<BookInfo> selectBookList(@Param("params") Map<String, Object> params);

    /**
     * 统计图书信息
     */
    Map<String, Object> countBooks(@Param("params") Map<String, Object> params);
}
