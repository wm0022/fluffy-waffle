package com.shengwei.tushuguanli.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shengwei.tushuguanli.entity.BookInfo;
import com.shengwei.tushuguanli.entity.CartItemVO;
import com.shengwei.tushuguanli.entity.ShoppingCart;
import com.shengwei.tushuguanli.mapper.ShoppingCartMapper;
import com.shengwei.tushuguanli.service.BookInfoService;
import com.shengwei.tushuguanli.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物车服务实现
 */
@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {

    @Autowired
    private BookInfoService bookInfoService;

    @Override
    public void addToCart(Long userId, Long bookId, Integer quantity) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
               .eq(ShoppingCart::getBookId, bookId);
        
        ShoppingCart existCart = getOne(wrapper);
        if (existCart != null) {
            existCart.setQuantity(quantity);
            updateById(existCart);
        } else {
            ShoppingCart cart = new ShoppingCart();
            cart.setUserId(userId);
            cart.setBookId(bookId);
            cart.setQuantity(quantity);
            save(cart);
        }
    }

    @Override
    public List<CartItemVO> getCartList(Long userId) {
        // 查询购物车列表
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
               .orderByDesc(ShoppingCart::getUpdateTime);
        List<ShoppingCart> cartList = list(wrapper);
        
        if (cartList.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有图书 ID
        List<Long> bookIds = cartList.stream()
                .map(ShoppingCart::getBookId)
                .collect(Collectors.toList());
        
        // 查询图书信息
        LambdaQueryWrapper<BookInfo> bookWrapper = new LambdaQueryWrapper<>();
        bookWrapper.in(BookInfo::getId, bookIds);
        List<BookInfo> books = bookInfoService.list(bookWrapper);
        
        // 将图书信息转为 Map 方便查找
        Map<Long, BookInfo> bookMap = books.stream()
                .collect(Collectors.toMap(BookInfo::getId, book -> book));
        
        // 组装购物车项 VO
        List<CartItemVO> result = new ArrayList<>();
        for (ShoppingCart cart : cartList) {
            CartItemVO vo = new CartItemVO();
            BeanUtils.copyProperties(cart, vo);
            
            BookInfo book = bookMap.get(cart.getBookId());
            if (book != null) {
                vo.setBookName(book.getBookName());
                vo.setAuthor(book.getAuthor());
                vo.setSellingPrice(book.getSellingPrice());
                vo.setOriginalPrice(book.getOriginalPrice());
                vo.setCoverImage(book.getCoverImage());
                // 库存数量
                vo.setStockCount(book.getStockCount());
                // 计算小计
                if (book.getSellingPrice() != null) {
                    vo.setSubtotal(book.getSellingPrice().multiply(new BigDecimal(cart.getQuantity())));
                }
            }
            
            result.add(vo);
        }
        
        return result;
    }

    @Override
    public void clearCart(Long userId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId);
        remove(wrapper);
    }

    @Override
    public void removeFromCart(Long userId, Long bookId) {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, userId)
               .eq(ShoppingCart::getBookId, bookId);
        remove(wrapper);
    }
}
