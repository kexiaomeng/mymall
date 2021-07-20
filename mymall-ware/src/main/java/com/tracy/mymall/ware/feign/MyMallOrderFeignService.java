package com.tracy.mymall.ware.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mymall-order")
public interface MyMallOrderFeignService {

    @RequestMapping("/order/order/query/{orderSn}")
    R queryOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}
