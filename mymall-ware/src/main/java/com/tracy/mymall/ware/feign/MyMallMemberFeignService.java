package com.tracy.mymall.ware.feign;

import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("mymall-member")
public interface MyMallMemberFeignService {
    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    public R info(@PathVariable("id") Long id);
//        MemberReceiveAddressEntity memberReceiveAddress = memberReceiveAddressService.getById(id);
//
//        return R.ok().put("memberReceiveAddress", memberReceiveAddress);
//    }
}
