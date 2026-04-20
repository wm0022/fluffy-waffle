package com.shengwei.tushuguanli.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shengwei.tushuguanli.common.PageResult;
import com.shengwei.tushuguanli.common.Result;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.service.BookInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 图书信息管理控制器
 */
@RestController
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookInfoService bookInfoService;

    /**
     * 分页查询图书列表
     */
    @GetMapping("/page")
    public Result<PageResult<BookInfo>> pageBookList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String bookName,
            String author,
            String isbn,
            String publisher,
            Long categoryId,
            Integer shelfStatus,
            Integer isHot,
            Integer isNew,
            Integer isDonation,
            String sortBy) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("bookName", bookName);
        params.put("author", author);
        params.put("isbn", isbn);
        params.put("publisher", publisher);
        params.put("categoryId", categoryId);
        params.put("shelfStatus", shelfStatus);
        params.put("isHot", isHot);
        params.put("isNew", isNew);
        params.put("isDonation", isDonation);
        params.put("sortBy", sortBy);

        Page<BookInfo> page = bookInfoService.pageBookList(pageNum, pageSize, params);
        PageResult<BookInfo> result = PageResult.of(
                page.getTotal(),
                page.getPages(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );

        return Result.success(result);
    }

    /**
     * 获取热销图书
     */
    @GetMapping("/hot")
    public Result<List<BookInfo>> getHotBooks(@RequestParam(defaultValue = "4") Integer limit) {
        List<BookInfo> books = bookInfoService.getHotBooks(limit);
        return Result.success(books);
    }

    /**
     * 获取新品图书
     */
    @GetMapping("/new")
    public Result<List<BookInfo>> getNewBooks(@RequestParam(defaultValue = "4") Integer limit) {
        List<BookInfo> books = bookInfoService.getNewBooks(limit);
        return Result.success(books);
    }

    /**
     * 根据 ID 查询图书详情
     */
    @GetMapping("/{bookId}")
    public Result<BookInfo> getBookById(@PathVariable Long bookId) {
        BookInfo bookInfo = bookInfoService.getById(bookId);
        if (bookInfo == null) {
            return Result.error("图书不存在");
        }
        return Result.success(bookInfo);
    }

    /**
     * 根据 ISBN 查询图书
     */
    @GetMapping("/isbn/{isbn}")
    public Result<BookInfo> getBookByIsbn(@PathVariable String isbn) {
        BookInfo bookInfo = bookInfoService.getByIsbn(isbn);
        if (bookInfo == null) {
            return Result.error("图书不存在");
        }
        return Result.success(bookInfo);
    }

    /**
     * 添加图书
     */
    @PostMapping
    public Result<Void> addBook(@RequestBody BookInfo bookInfo) {
        bookInfoService.addBook(bookInfo);
        return Result.success("图书添加成功");
    }

    /**
     * 更新图书
     */
    @PutMapping
    public Result<Void> updateBook(@RequestBody BookInfo bookInfo) {
        bookInfoService.updateBook(bookInfo);
        return Result.success("图书更新成功");
    }

    /**
     * 删除图书
     */
    @DeleteMapping("/{bookId}")
    public Result<Void> deleteBook(@PathVariable Long bookId) {
        bookInfoService.deleteBook(bookId);
        return Result.success("图书删除成功");
    }

    /**
     * 上下架图书
     */
    @PutMapping("/{bookId}/shelf")
    public Result<Void> updateShelfStatus(
            @PathVariable Long bookId,
            @RequestParam Integer shelfStatus) {
        bookInfoService.updateShelfStatus(bookId, shelfStatus);
        return Result.success("操作成功");
    }

    /**
     * 图书统计
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> countBooks() {
        Map<String, Object> result = bookInfoService.countBooks(null);
        return Result.success(result);
    }

    /**
     * 获取所有图书列表（不分页）
     */
    @GetMapping("/list")
    public Result<List<BookInfo>> listBooks() {
        List<BookInfo> list = bookInfoService.list();
        return Result.success(list);
    }
}
