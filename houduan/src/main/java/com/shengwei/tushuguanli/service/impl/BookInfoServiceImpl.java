package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.DonationApply;
import com.shengwei.tushuguanli.entity.Inventory;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.service.BookInfoService;
import com.shengwei.tushuguanli.service.DonationApplyService;
import com.shengwei.tushuguanli.service.DonationService;
import com.shengwei.tushuguanli.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书信息服务实现
 */
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private DonationApplyService donationApplyService;

    @Override
    public Page<BookInfo> pageBookList(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        Page<BookInfo> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        
        if (params != null) {
            String bookName = (String) params.get("bookName");
            String author = (String) params.get("author");
            String isbn = (String) params.get("isbn");
            String publisher = (String) params.get("publisher");
            Long categoryId = (Long) params.get("categoryId");
            Integer shelfStatus = (Integer) params.get("shelfStatus");

            boolean hasSearch = StringUtils.hasText(bookName) || StringUtils.hasText(author) || StringUtils.hasText(publisher);
            if (hasSearch) {
                wrapper.and(w -> w.like(StringUtils.hasText(bookName), BookInfo::getBookName, bookName)
                        .or()
                        .like(StringUtils.hasText(author), BookInfo::getAuthor, author)
                        .or()
                        .like(StringUtils.hasText(publisher), BookInfo::getPublisher, publisher));
            }

            wrapper.eq(StringUtils.hasText(isbn), BookInfo::getIsbn, isbn)
                    .eq(categoryId != null, BookInfo::getCategoryId, categoryId)
                    .eq(shelfStatus != null, BookInfo::getShelfStatus, shelfStatus);

            Integer isDonation = (Integer) params.get("isDonation");
            wrapper.eq(isDonation != null, BookInfo::getIsDonation, isDonation);
        }

        String sortBy = (String) (params != null ? params.get("sortBy") : null);
        if ("sales".equals(sortBy)) {
            wrapper.orderByDesc(BookInfo::getSalesVolume);
        } else if ("price".equals(sortBy)) {
            wrapper.orderByAsc(BookInfo::getSellingPrice);
        } else if ("time".equals(sortBy)) {
            wrapper.orderByDesc(BookInfo::getShelfTime);
        } else {
            wrapper.orderByDesc(BookInfo::getCreateTime);
        }
        return page(page, wrapper);
    }

    @Override
    public List<BookInfo> getHotBooks(Integer limit) {
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getShelfStatus, 1)
               .orderByDesc(BookInfo::getSalesVolume)
               .last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public List<BookInfo> getNewBooks(Integer limit) {
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getShelfStatus, 1)
               .orderByDesc(BookInfo::getShelfTime)
               .last("LIMIT " + limit);
        return list(wrapper);
    }

    @Override
    public BookInfo getByIsbn(String isbn) {
        if (!StringUtils.hasText(isbn)) {
            return null;
        }
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getIsbn, isbn);
        return getOne(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBook(BookInfo bookInfo) {
        if (StringUtils.hasText(bookInfo.getIsbn())) {
            BookInfo existBook = getByIsbn(bookInfo.getIsbn());
            if (existBook != null) {
                throw new BusinessException("该 ISBN 图书已存在");
            }
        }
        
        save(bookInfo);

        inventoryService.createInventory(bookInfo.getId(), bookInfo.getStockCount());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBook(BookInfo bookInfo) {
        BookInfo existBook = getById(bookInfo.getId());
        if (existBook == null) {
            throw new BusinessException("图书不存在");
        }

        String existIsbn = existBook.getIsbn();
        String newIsbn = bookInfo.getIsbn();
        if (newIsbn != null && !newIsbn.equals(existIsbn)) {
            BookInfo isbnExist = getByIsbn(newIsbn);
            if (isbnExist != null && !isbnExist.getId().equals(bookInfo.getId())) {
                throw new BusinessException("该 ISBN 已被其他图书使用");
            }
        }

        updateById(bookInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBook(Long id) {
        BookInfo bookInfo = getById(id);
        if (bookInfo == null) {
            throw new BusinessException("图书不存在");
        }

        removeById(id);
        inventoryService.deleteByBookId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShelfStatus(Long id, Integer shelfStatus) {
        BookInfo bookInfo = getById(id);
        if (bookInfo == null) {
            throw new BusinessException("图书不存在");
        }

        bookInfo.setShelfStatus(shelfStatus);
        updateById(bookInfo);
    }

    @Override
    public Map<String, Object> countBooks(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        long totalCount = count();
        result.put("totalCount", totalCount);

        long onShelfCount = count(new LambdaQueryWrapper<BookInfo>()
                .eq(BookInfo::getShelfStatus, 1));
        result.put("onShelfCount", onShelfCount);

        long offShelfCount = totalCount - onShelfCount;
        result.put("offShelfCount", offShelfCount);

        long hotBookCount = count(new LambdaQueryWrapper<BookInfo>()
                .gt(BookInfo::getSalesVolume, 0));
        result.put("hotBookCount", hotBookCount);

        long newBookCount = count(new LambdaQueryWrapper<BookInfo>()
                .orderByDesc(BookInfo::getShelfTime)
                .last("LIMIT 30"));
        result.put("newBookCount", newBookCount);
        
        Map<String, Object> donationStats = donationService.countDonations();
        result.put("donationBookCount", donationStats.getOrDefault("total", 0L));

        return result;
    }
}
