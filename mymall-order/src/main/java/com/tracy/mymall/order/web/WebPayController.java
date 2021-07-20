package com.tracy.mymall.order.web;

import com.tracy.mymall.order.configuration.AliPayTemplate;
import com.tracy.mymall.order.service.OrderService;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebPayController {


    @Autowired
    private OrderService orderService;

    /**
     * 订单支付，调用支付宝接口支付
     * @param orderSn
     * @return
     */
    @GetMapping(value = "/payOrder", produces = "text/html")
    @ResponseBody
    public String payOrder(@PathVariable("orderSn")String orderSn) {
       return orderService.payOrder(orderSn);
    }
}
