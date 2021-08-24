package com.tracy.mymall.product.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("mymall-secondkill")
public interface MymallSeckillFeignService {
    @GetMapping("/seckill/sku/{skuId}")
    public R getSkuSecondkillMsg(@PathVariable("skuId")Long skuId);
}
