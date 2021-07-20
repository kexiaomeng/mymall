package com.tracy.mymall.cart.service;

import com.tracy.mymall.cart.vo.Cart;
import com.tracy.mymall.cart.vo.CartItem;
import com.tracy.mymall.cart.vo.UserInfoTo;

import java.util.List;

public interface CartService {
    CartItem addToCart(Long skuId, int num);

    CartItem queryFromCart(Long skuId);

    Cart getCart(UserInfoTo userInfoTo);

    void checkCart(Long skuId, Integer isChecked);

    void deleteCartItem(Long skuId);

    void countItem(Long skuId, int num);

    List<CartItem> getCheckedItems();
}
