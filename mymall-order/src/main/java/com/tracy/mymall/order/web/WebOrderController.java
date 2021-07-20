package com.tracy.mymall.order.web;

import com.tracy.mymall.order.entity.OrderEntity;
import com.tracy.mymall.order.service.OrderService;
import com.tracy.mymall.order.vo.OrderConfirmVo;
import com.tracy.mymall.order.vo.OrderSubmitRespVo;
import com.tracy.mymall.order.vo.OrderSubmitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{path}.html")
    public String test(@PathVariable("path")String path) {
        return  path;
    }

    /**
     * 查询订单交易相关的数据，提供给页面展示
     * @return
     */
    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("confirmOrder", orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String orderSubmit(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        OrderSubmitRespVo orderSubmitRespVo = null;
        try {
            orderSubmitRespVo = orderService.submitOrder(orderSubmitVo);

        }catch(Exception e) {
            orderSubmitRespVo = new OrderSubmitRespVo();
            orderSubmitRespVo.setCode(3);
        }
        // 如果没错，则直接跳转到付款界面
        if (orderSubmitRespVo.getCode() == 0) {
            OrderEntity order = orderSubmitRespVo.getOrder();
            model.addAttribute("order", order);
            return "pay";

        }else {
            String msg = "";
            switch (orderSubmitRespVo.getCode()) {
                case 1:
                    msg = "订单信息失效，请重新提交";
                    break;
                case 2:
                    msg = "购物车中商品价格发生变化，请确认后重新提交";
                    break;
                case 3:
                    msg = "库存不足，请检查";
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            // 如果有错，则跳转到重新结算界面
            return "redirect:http://order.mymall.com:1111/toTrade";
        }
    }
}
