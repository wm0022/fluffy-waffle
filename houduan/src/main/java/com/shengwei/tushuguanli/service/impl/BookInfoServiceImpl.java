package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.DonationApply;
import com.shengwei.tushuguanli.entity.Inventory;
import com.shengwei.tushuguanli.exception.BusinessException;
import com.shengwei.tushuguanli.mapper.BookInfoMapper;
import com.shengwei.tushuguanli.service.BookInfoService;
import com.shengwei.tushuguanli.service.DonationApplyService;
import com.shengwei.tushuguanli.service.DonationService;
import com.shengwei.tushuguanli.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 图书信息服务实现（含 Redis 缓存）
 */
@Service
public class BookInfoServiceImpl extends ServiceImpl<BookInfoMapper, BookInfo> implements BookInfoService {

    private static final Logger log = LoggerFactory.getLogger(BookInfoServiceImpl.class);

    // ==================== Redis 缓存 Key 前缀 ====================
    private static final String CACHE_KEY_HOT = "book:hot:";
    private static final String CACHE_KEY_NEW = "book:new:";
    private static final String CACHE_KEY_DETAIL = "book:detail:";
    private static final String CACHE_KEY_PAGE = "book:page:";

    // ==================== 缓存过期时间（秒） ====================
    private static final long HOT_CACHE_TTL = 300;       // 热销 5分钟
    private static final long NEW_CACHE_TTL = 300;        // 新品 5分钟
    private static final long DETAIL_CACHE_TTL = 900;     // 详情 15分钟
    private static final long PAGE_CACHE_TTL = 180;       // 分页列表 3分钟

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private DonationService donationService;

    @Autowired
    private DonationApplyService donationApplyService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private ObjectMapper objectMapper = new ObjectMapper() {{
        // 注册 Java 8 时间类型模块（LocalDate/LocalDateTime 等）
        registerModule(new JavaTimeModule());
    }};

    // ==================== 查询方法（带缓存） ====================

    /**
     * 用 inventory 表的真实库存覆盖 bookInfo 的 stockCount，确保两模块数据一致
     */
    private void syncStockFromInventory(BookInfo bookInfo) {
        if (bookInfo != null && bookInfo.getId() != null) {
            try {
                Inventory inv = inventoryService.getInventoryByBookId(bookInfo.getId());
                if (inv != null) {
                    bookInfo.setStockCount(inv.getStockQuantity());
                }
            } catch (Exception e) {
                log.warn("同步库存异常, bookId={}: {}", bookInfo.getId(), e.getMessage());
            }
        }
    }

    /**
     * 重写 getById，加入 Redis 缓存（图书详情页高频接口）
     */
    @Override
    public BookInfo getById(Serializable id) {
        String cacheKey = CACHE_KEY_DETAIL + id;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("图书详情命中缓存 key={}", cacheKey);
                return objectMapper.convertValue(cached, BookInfo.class);
            }
        } catch (Exception e) {
            log.warn("读取图书详情缓存异常: {}", e.getMessage());
        }

        // 缓存未命中，查库
        BookInfo bookInfo = super.getById(id);

        // 存在才写入缓存 + 同步库存
        if (bookInfo != null) {
            syncStockFromInventory(bookInfo);
            try {
                redisTemplate.opsForValue().set(cacheKey, bookInfo, DETAIL_CACHE_TTL, TimeUnit.SECONDS);
                log.debug("图书详情已写入缓存 key={}, ttl={}s", cacheKey, DETAIL_CACHE_TTL);
            } catch (Exception e) {
                log.warn("写入图书详情缓存异常: {}", e.getMessage());
            }
        }

        return bookInfo;
    }

    @Override
    public Page<BookInfo> pageBookList(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        // 构建缓存 key：将查询参数拼接成唯一标识
        String cacheKey = buildPageCacheKey(pageNum, pageSize, params);

        try {
            // 1. 尝试从 Redis 获取缓存（Jackson 序列化器自动反序列化）
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("图书分页列表命中缓存 key={}", cacheKey);
                return objectMapper.convertValue(cached, new TypeReference<Page<BookInfo>>() {});
            }
        } catch (Exception e) {
            log.warn("读取图书分页缓存异常，走数据库查询: {}", e.getMessage());
        }

        // 2. 缓存未命中，查库
        Page<BookInfo> page = queryFromDb(pageNum, pageSize, params);

        // 3. 写入缓存（Jackson 序列化器自动处理序列化）
        try {
            redisTemplate.opsForValue().set(cacheKey, page, PAGE_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("图书分页列表已写入缓存 key={}, ttl={}s", cacheKey, PAGE_CACHE_TTL);
        } catch (Exception e) {
            log.warn("写入图书分页缓存异常（不影响业务）: {}", e.getMessage());
        }

        return page;
    }

    /**
     * 实际的数据库查询逻辑（从原方法抽取）
     */
    private Page<BookInfo> queryFromDb(Integer pageNum, Integer pageSize, Map<String, Object> params) {
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
        Page<BookInfo> result = page(page, wrapper);
        // 分页列表每条记录同步 inventory 真实库存，保证与库存管理一致
        result.getRecords().forEach(this::syncStockFromInventory);
        return result;
    }

    @Override
    public List<BookInfo> getHotBooks(Integer limit) {
        String cacheKey = CACHE_KEY_HOT + limit;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("热销图书命中缓存 key={}", cacheKey);
                return objectMapper.convertValue(cached, new TypeReference<List<BookInfo>>() {});
            }
        } catch (Exception e) {
            log.warn("读取热销缓存异常: {}", e.getMessage());
        }

        // 查库：按 is_hot 标记筛选热销图书，按创建时间降序（最新的排前面）
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getShelfStatus, 1)
               .eq(BookInfo::getIsHot, 1)
               .orderByDesc(BookInfo::getCreateTime)
               .last("LIMIT " + limit);
        List<BookInfo> books = list(wrapper);
        books.forEach(this::syncStockFromInventory);

        // 写入缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, books, HOT_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("热销图书已写入缓存 key={}", cacheKey);
        } catch (Exception e) {
            log.warn("写入热销缓存异常: {}", e.getMessage());
        }

        return books;
    }

    @Override
    public List<BookInfo> getNewBooks(Integer limit) {
        String cacheKey = CACHE_KEY_NEW + limit;

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.debug("新品图书命中缓存 key={}", cacheKey);
                return objectMapper.convertValue(cached, new TypeReference<List<BookInfo>>() {});
            }
        } catch (Exception e) {
            log.warn("读取新品缓存异常: {}", e.getMessage());
        }

        // 查库：按 is_new 标记筛选新品图书，按创建时间降序（最新的排前面）
        LambdaQueryWrapper<BookInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookInfo::getShelfStatus, 1)
               .eq(BookInfo::getIsNew, 1)
               .orderByDesc(BookInfo::getCreateTime)
               .last("LIMIT " + limit);
        List<BookInfo> books = list(wrapper);
        books.forEach(this::syncStockFromInventory);

        // 写入缓存
        try {
            redisTemplate.opsForValue().set(cacheKey, books, NEW_CACHE_TTL, TimeUnit.SECONDS);
            log.debug("新品图书已写入缓存 key={}", cacheKey);
        } catch (Exception e) {
            log.warn("写入新品缓存异常: {}", e.getMessage());
        }

        return books;
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

    // ==================== 写操作（带缓存清除） ====================

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

        // 清除所有图书相关缓存
        clearAllBookCache();
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

        // 库存双向同步：检测 stockCount 变化并同步到 inventory 表
        Integer oldStock = existBook.getStockCount();
        Integer newStock = bookInfo.getStockCount();
        if (newStock != null && oldStock != null && !oldStock.equals(newStock)) {
            int diff = newStock - oldStock;
            try {
                if (diff > 0) {
                    inventoryService.increaseStock(bookInfo.getId(), diff);
                } else {
                    inventoryService.decreaseStock(bookInfo.getId(), Math.abs(diff));
                }
            } catch (Exception e) {
                log.warn("同步inventory表库存异常, bookId={}: {}", bookInfo.getId(), e.getMessage());
            }
        }

        updateById(bookInfo);

        // 清除所有图书相关缓存（含详情缓存）
        clearAllBookCache();
        clearDetailCache(bookInfo.getId());
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

        // 清除所有图书相关缓存
        clearAllBookCache();
        clearDetailCache(id);
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

        // 上下架影响热销/新品/分页/详情
        clearAllBookCache();
        clearDetailCache(id);
    }

    @Override
    public Map<String, Object> countBooks(Map<String, Object> params) {
        Map<String, Object> result = new java.util.HashMap<>();

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

    // ==================== 缓存 Key 构建 / 清除工具方法 ====================

    /**
     * 构建分页查询的缓存 Key
     */
    private String buildPageCacheKey(Integer pageNum, Integer pageSize, Map<String, Object> params) {
        StringBuilder sb = new StringBuilder(CACHE_KEY_PAGE);
        sb.append(pageNum).append(":").append(pageSize).append(":");
        if (params != null) {
            sb.append(params.hashCode());
        }
        return sb.toString();
    }

    /**
     * 清除所有图书列表类缓存（热销、新品、分页）
     */
    private void clearAllBookCache() {
        try {
            // 批量删除匹配前缀的 key
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_HOT + "*"));
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_NEW + "*"));
            redisTemplate.delete(redisTemplate.keys(CACHE_KEY_PAGE + "*"));
            log.info("已清除图书列表缓存 (hot/new/page)");
        } catch (Exception e) {
            log.warn("清除图书列表缓存异常: {}", e.getMessage());
        }
    }

    /**
     * 清除单本图书详情缓存
     */
    private void clearDetailCache(Long bookId) {
        try {
            Boolean deleted = redisTemplate.delete(CACHE_KEY_DETAIL + bookId);
            if (Boolean.TRUE.equals(deleted)) {
                log.info("已清除图书详情缓存 bookId={}", bookId);
            }
        } catch (Exception e) {
            log.warn("清除图书详情缓存异常: {}", e.getMessage());
        }
    }
}
