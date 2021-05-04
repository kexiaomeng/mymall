package com.tracy.mymall.product.feign;

import com.tracy.mymall.common.dto.SkuReductionDto;
import com.tracy.mymall.common.dto.SpuBoundsDto;
import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "mymall-coupon")
public interface CouponFeignService {
    @PostMapping("/coupon/spubounds/save")
    R saveSpuBounds(@RequestBody SpuBoundsDto spuBounds);

    @PostMapping("coupon/skufullreduction/saveInfo")
    R saveSkuReductionTo(@RequestBody SkuReductionDto skuReductionDto);
}
