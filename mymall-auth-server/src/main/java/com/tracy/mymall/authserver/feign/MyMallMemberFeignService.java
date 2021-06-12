package com.tracy.mymall.authserver.feign;

import com.tracy.mymall.authserver.vo.LoginVo;
import com.tracy.mymall.authserver.vo.SocialRespVo;
import com.tracy.mymall.authserver.vo.UserRegisterVo;
import com.tracy.mymall.common.exception.RRException;
import com.tracy.mymall.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("mymall-member")
public interface MyMallMemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo userRegisterVo);
    @PostMapping("/member/member/login")
    R login(@RequestBody LoginVo memberLoginDto) ;
    @PostMapping("/member/member/social/login")
    R socialLogin(SocialRespVo socialRespVo);


}
