package com.tracy.mymall.member.feign.fallback;

import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.member.feign.RemoteCouponService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RemoteCouponFallbackService implements RemoteCouponService {
    @Override
    public R memberCouponList(Map<String, Object> params) {
        return R.error("查询失败").put("coupon","无");
    }
}
