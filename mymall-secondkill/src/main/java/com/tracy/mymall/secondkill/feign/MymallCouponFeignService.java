package com.tracy.mymall.secondkill.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("mymall-coupon")
public interface MymallCouponFeignService {
    @GetMapping("/coupon/seckillsession/3daysList")
    R getLatest3DaysSession();
}
