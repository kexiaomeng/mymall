package com.tracy.mymall.search.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mymall-product")
public interface ProductFeignService {
    @RequestMapping("/product/attr/info/{attrId}")
    R info(@PathVariable("attrId") Long attrId);
}
