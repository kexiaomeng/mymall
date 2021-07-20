package com.tracy.mymall.order.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mymall-product")
public interface MyMallProductFeignService {
    @RequestMapping("/product/skuinfo/spuinfoBySku/{skuId}")
    R spuinfoBysku(@PathVariable("skuId") Long skuId);
}
