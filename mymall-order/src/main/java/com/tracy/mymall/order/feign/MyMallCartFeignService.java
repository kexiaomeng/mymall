package com.tracy.mymall.order.feign;

import com.tracy.mymall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient("mymall-cart")
public interface MyMallCartFeignService {
    @GetMapping("/checkedItems")
    List<OrderItemVo> getCheckedItems() ;
}
