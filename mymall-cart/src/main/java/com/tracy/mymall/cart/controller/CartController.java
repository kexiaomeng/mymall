package com.tracy.mymall.cart.controller;

import com.tracy.mymall.cart.interceptor.CartInterceptor;
import com.tracy.mymall.cart.service.CartService;
import com.tracy.mymall.cart.vo.Cart;
import com.tracy.mymall.cart.vo.CartItem;
import com.tracy.mymall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Cart cart = cartService.getCart(userInfoTo);
        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * 此处应该重定向到success，不应该forword防止用户在页面重复点击刷新导致重复提交多次增加购物项,
     * 重定向是时两次请求，所以不可以使用同一个model，可以使用redirectAttribute
     * @param skuId
     * @param num
     * @param model
     * @return
     */
    @GetMapping("addCartItem")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num")int num,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        CartItem cartItem = cartService.addToCart(skuId, num);
        model.addAttribute("cartItem", cartItem);
//        return "success";
        redirectAttributes.addAttribute("skuId", skuId);
        return "redirect:http://cart.mymall.com:1111/success.html";
    }

    @GetMapping("/success.html")
    public String addToCart(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.queryFromCart(skuId);

        model.addAttribute("cartItem", cartItem);

//        CartItem cartItem = cartService.addToCart(skuId, num);
//        model.addAttribute("cartItem", cartItem);

        return "success";
    }


    @PostMapping("/checkCart")
    public String checkCart(@RequestBody CartItem cartItem) {
//        cartService.checkCart(skuId, isChecked);

        // 重定向到购物车列表页
        return "redirect:http://cart.mymall.com:1111/cart.html";

    }

    @GetMapping("/deleteItem")
    public String deleteCartItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteCartItem(skuId);

        // 重定向到购物车列表页
        return "redirect:http://cart.mymall.com:1111/cart.html";

    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);

        // 重定向到购物车列表页
        return "redirect:http://cart.mymall.com:1111/cart.html";

    }

    @GetMapping("/checkedItems")
    @ResponseBody
    public List<CartItem> getCheckedItems() {
        List<CartItem> checkedItems = cartService.getCheckedItems();
        return checkedItems;


    }
}
