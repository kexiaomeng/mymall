package com.tracy.mymall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tracy.mymall.cart.feign.MyMallProductFeignService;
import com.tracy.mymall.cart.interceptor.CartInterceptor;
import com.tracy.mymall.cart.service.CartService;
import com.tracy.mymall.cart.vo.Cart;
import com.tracy.mymall.cart.vo.CartItem;
import com.tracy.mymall.cart.vo.UserInfoTo;
import com.tracy.mymall.common.dto.SkuEntityDto;
import com.tracy.mymall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Component
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private MyMallProductFeignService myMallProductFeignService;

    private final static String CART_REDIS_KEY = "mymall:cart:";
    /**
     * 根据用户是否登录选择redis的key
     * 添加到购物车，构建CartItem，保存到redis中
     * @param skuId
     * @param num
     * @return
     */
    @Override
    public CartItem addToCart(Long skuId, int num) {
        // 根据用户是否登录选择redis的key
        BoundHashOperations<String, Object, Object> hashOps = getCartRedisKey();
        String cartMsg = (String) hashOps.get(skuId.toString());
        CartItem cartItem;
        // 判断购物项是否已经存在
        if (StringUtils.isEmpty(cartMsg)) {
            // 构建请求，异步编排
            cartItem = new CartItem();
            cartItem.setCheck(true);
            cartItem.setSkuId(skuId);
            cartItem.setCount(num);
            // 1. 请求商品sku信息
            CompletableFuture<Void> skuInfo = CompletableFuture.runAsync(() -> {
                R sku = myMallProductFeignService.info(skuId);
                if (sku.getCode() == 0) {
                    SkuEntityDto skuEntityDto = JSON.parseObject(JSON.toJSONString(sku.get("skuInfo")), new TypeReference<SkuEntityDto>(){});
                    cartItem.setImage(skuEntityDto.getSkuDefaultImg());
                    cartItem.setPrice(skuEntityDto.getPrice());
                    cartItem.setTitle(skuEntityDto.getSkuTitle());
                }

            }, threadPoolExecutor);

            // 2. 请求商品saleAttr信息
            CompletableFuture<Void> saleAttrs = CompletableFuture.runAsync(() -> {
                // sql：可以把两列的值合并成一个string，多行就有多个值
                // SELECT CONCAT(P.ATTR_NAME, ":", P.ATTR_VALUE) FROM PMS_SKU_SALE_ATTR_VALUE P WHERE P.SKU_ID=1;
                List<String> values = myMallProductFeignService.getSkuSaleAttrValues(skuId);
                cartItem.setSkuAttr(values);
            }, threadPoolExecutor);

            try {
                CompletableFuture.allOf(skuInfo, saleAttrs).get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }else {
            cartItem = JSONObject.parseObject(cartMsg, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
        }


        String redisValue = JSONObject.toJSONString(cartItem);
        hashOps.put(skuId.toString(), redisValue);
        return cartItem;
    }

    @Override
    public CartItem queryFromCart(Long skuId) {
        BoundHashOperations<String, Object, Object> redisKey = getCartRedisKey();
        String value = (String)redisKey.get(skuId.toString());
        return JSONObject.parseObject(value, CartItem.class);

    }

    /**
     * 获取购物车信息
     * 登录/未登录
     * 如果登录了，则需要判断是否有临时购物车，将临时购物车的数据合并到登录购物车，删除临时购物车
     * @param userInfoTo
     * @return
     */
    @Override
    public Cart getCart(UserInfoTo userInfoTo) {
        Cart cart = new Cart();
        // 判断是否登录
        if (userInfoTo.getUserId() != null) {
            // 登录，查询临时购物车是否有数据，有的话需要合并
            String key = CART_REDIS_KEY + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(key);
            if (cartItems != null && cartItems.size() > 0) {
                for (CartItem item: cartItems) {
                    // 将item加到登录购物车当中
                    addToCart(item.getSkuId(), item.getCount());
                }
                // 清空临时购物车
                clearCart(key);
            }
            key = CART_REDIS_KEY + userInfoTo.getUserId();

            cartItems = getCartItems(key);
            cart.setItems(cartItems);
        }else {
            // 没有登录，查询临时购物车
            String key = CART_REDIS_KEY + userInfoTo.getUserKey();
            List<CartItem> cartItems = getCartItems(key);
            cart.setItems(cartItems);
        }
        return cart;
    }

    @Override
    public void checkCart(Long skuId, Integer isChecked) {
        BoundHashOperations<String, Object, Object> cartRedisKey = getCartRedisKey();

        CartItem cartItem = queryFromCart(skuId);
        cartItem.setCheck(isChecked == 1 ? true : false);
        cartRedisKey.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartRedisKey = getCartRedisKey();
        cartRedisKey.delete(skuId.toString());
    }

    @Override
    public void countItem(Long skuId, int num) {
        BoundHashOperations<String, Object, Object> cartRedisKey = getCartRedisKey();

        CartItem cartItem = queryFromCart(skuId);
        cartItem.setCount(num);

        cartRedisKey.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public List<CartItem> getCheckedItems() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        if (userInfo.getUserId() == null) {
            log.error("【购物车】获取用户选定的购物项，但用户没有登录");
            return null;
        }
        String key = CART_REDIS_KEY + userInfo.getUserId();
        List<CartItem> cartItems = getCartItems(key);
        List<CartItem> checkedItems = cartItems.stream().filter(CartItem::getCheck).map(item -> {
            // 需要远程查询商品的当前价格
//            item.setPrice(xxxxx);
            return item;
        }).collect(Collectors.toList());


        return checkedItems;
    }

    private void clearCart(String key) {
        redisTemplate.delete(key);
    }

    List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> cartRedisKey = redisTemplate.boundHashOps(cartKey);

        List<CartItem> cartItems = null;
        List<Object> values = cartRedisKey.values();
        if (values != null && values.size() > 0) {
            cartItems = values.stream().map(item -> {
                String itemString = (String)item;
                CartItem cartItem = JSON.parseObject(itemString, CartItem.class);
                return cartItem;
            }).collect(Collectors.toList());

        }
        return cartItems;
    }

    /**
     * // 根据用户是否登录选择redis的key
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartRedisKey() {
        String key = "";
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        if (userInfoTo.getUserId() != null) {
            key = CART_REDIS_KEY + userInfoTo.getUserId();
        }else {
            key = CART_REDIS_KEY + userInfoTo.getUserKey();

        }
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        return hashOps;
    }
}
