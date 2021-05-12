package com.tracy.mymall.ware.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/*
访问网关
 */
@FeignClient("mymall-gateway")
public interface ProductFeignService {
    /**
     * 访问网关时需要看是否有重写路径，如果重写路径，则需要加上重写之间的路径
     * @param skuId
     * @return
     */
    @RequestMapping("/api/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
