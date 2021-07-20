package com.tracy.mymall.order.feign;

import com.tracy.mymall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient("mymall-member")
public interface MyMallMemberFeignService {
    @RequestMapping("/member/memberreceiveaddress/{memberid}/address")
    List<MemberAddressVo> getMemberAddress(@PathVariable("memberid") Long memberid);
}
