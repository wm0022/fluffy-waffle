package com.shengwei.tushuguanli.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shengwei.tushuguanli.entity.BookInfo;

import java.util.List;
import java.util.Map;

/**
 * 图书信息服务
 */
public interface BookInfoService extends IService<BookInfo> {

    /**
     * 分页查询图书列表
     */
    Page<BookInfo> pageBookList(Integer pageNum, Integer pageSize, Map<String, Object> params);

    /**
     * 获取热销图书
     */
    List<BookInfo> getHotBooks(Integer limit);

    /**
     * 获取新品图书
     */
    List<BookInfo> getNewBooks(Integer limit);

    /**
     * 根据 ISBN 查询图书
     */
    BookInfo getByIsbn(String isbn);

    /**
     * 添加图书
     */
    void addBook(BookInfo bookInfo);

    /**
     * 更新图书
     */
    void updateBook(BookInfo bookInfo);

    /**
     * 删除图书
     */
    void deleteBook(Long bookId);

    /**
     * 上下架图书
     */
    void updateShelfStatus(Long bookId, Integer shelfStatus);

    /**
     * 统计图书信息
     */
    Map<String, Object> countBooks(Map<String, Object> params);
}
