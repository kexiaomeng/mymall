package com.tracy.mymall.member.feign;

import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.member.feign.fallback.RemoteCouponFallbackService;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Component
@FeignClient(value = "mymall-coupon", fallback = RemoteCouponFallbackService.class)
public interface RemoteCouponService {
    @RequestMapping("/coupon/coupon/member/list")
    R memberCouponList(@RequestParam Map<String, Object> params);

}
