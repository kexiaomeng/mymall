package com.tracy.mymall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.tracy.mymall.authserver.feign.MyMallMemberFeignService;
import com.tracy.mymall.authserver.vo.SocialAuthAccessKayObject;
import com.tracy.mymall.authserver.vo.SocialRespVo;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.common.vo.MemberEntityVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;

@Controller
@Slf4j
public class Oauth2Controller {

    @Autowired
    private MyMallMemberFeignService myMallMemberFeignService;

    @Autowired
    private RestTemplate restTemplate;
    @GetMapping("/oauth/weibo/success")
    public String weiboLogin(@RequestParam("code") String code, HttpSession session) {
        // 根据返回得code获取token
        SocialAuthAccessKayObject socialAuthAccessKayObject = new SocialAuthAccessKayObject();
        socialAuthAccessKayObject.setCode(code);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("client_id", "2635041892");
        map.add("client_secret", "4b75226f62784255535bd606547c22c1");
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", "http://auth.mymall.com:1111/oauth/weibo/success");
        map.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, null);

        ResponseEntity responseEntity = restTemplate.postForEntity("https://api.weibo.com/oauth2/access_token", request, SocialRespVo.class);
        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            SocialRespVo socialRespVo = (SocialRespVo) responseEntity.getBody();

            // 相当于我们知道了当前是那个用户
            // 1.如果用户是第一次进来 自动注册进来(为当前社交用户生成一个会员信息 以后这个账户就会关联这个账号)
            R login = myMallMemberFeignService.socialLogin(socialRespVo);
            if (login.getCode() == 0) {

                String msg = (String) login.get("msg");
                MemberEntityVo memberEntityVo = JSON.parseObject(msg, MemberEntityVo.class);
                log.info("\n欢迎 [" + memberEntityVo.getUsername() + "] 使用社交账号登录");

//                MemberRsepVo rsepVo = login.getData("data" ,new TypeReference<MemberRsepVo>() {});
//
//                log.info("\n欢迎 [" + rsepVo.getUsername() + "] 使用社交账号登录");
//                // 第一次使用session 命令浏览器保存这个用户信息 JESSIONSEID 每次只要访问这个网站就会带上这个cookie
//                // 在发卡的时候扩大session作用域 (指定域名为父域名)
                // TODO 1.默认发的当前域的session (需要解决子域session共享问题)
                // TODO 2.使用JSON的方式序列化到redis
//                				new Cookie("JSESSIONID","").setDomain("gulimall.com");
//                session.setAttribute(AuthServerConstant.LOGIN_USER, rsepVo);

                session.setAttribute("loginUser", memberEntityVo);
                return "redirect:http://mymall.com:1111";
            }else {
                return "redirect:http://auth.mymall.com:1111/login.html";

            }
        }else {
            return "redirect:http://auth.mymall.com:1111/login.html";

        }
    }
}
