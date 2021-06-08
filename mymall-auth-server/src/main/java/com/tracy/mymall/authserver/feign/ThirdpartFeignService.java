package com.tracy.mymall.authserver.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("mymall-thirdpart")
public interface ThirdpartFeignService {

    @GetMapping("/sms/sendCode")
    R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
