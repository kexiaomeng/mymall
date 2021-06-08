package com.tracy.mymall.authserver.controller;

import com.tracy.mymall.authserver.feign.MyMallMemberFeignService;
import com.tracy.mymall.authserver.feign.ThirdpartFeignService;
import com.tracy.mymall.authserver.vo.UserRegisterVo;
import com.tracy.mymall.common.constant.AuthConst;
import com.tracy.mymall.common.exception.ExceptionEnum;
import com.tracy.mymall.common.utils.R;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class LoginController {
// 可以用视图解析器的方式实现只返回视图名的页面
//    @GetMapping("/login.html")
//    public String login() {
//        return "login";
//    }
//// 可以用视图解析器的方式实现只返回视图名的页面
//    @GetMapping("/reg.html")
//    public String reg() {
//        return "reg";
//    }

    @Autowired
    private ThirdpartFeignService thirdpartFeignService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MyMallMemberFeignService myMallMemberFeignService;

    @ResponseBody
    @GetMapping("/sms/sendCode")
    public R sendCode(@RequestParam("phone") String phone) {
        // 1. 防止频繁掉用接口,在redis中存储时需要带上时间戳，根据时间戳判断
        String smsCodeRedis = redisTemplate.opsForValue().get(AuthConst.SMS_REDIS_PREFIX_CONST + phone);
        if (smsCodeRedis != null) {
            String[] splitCodeRedis = smsCodeRedis.split("_");
            long sendTimeStame = Long.parseLong(splitCodeRedis[1]);
            if (System.currentTimeMillis() - sendTimeStame < 60 * 1000L) {
                return R.error(ExceptionEnum.SMS_CODE_GET_FREQUENTLY.getErrorCode(), ExceptionEnum.SMS_CODE_GET_FREQUENTLY.getDesc());
            }

        }
        // 2. 验证码存储到reids中需要设置过期时间
        String code = UUID.randomUUID().toString().substring(0,6);
        redisTemplate.opsForValue().set(AuthConst.SMS_REDIS_PREFIX_CONST + phone, code+"_"+System.currentTimeMillis(), 10, TimeUnit.MINUTES );
        return thirdpartFeignService.sendSmsCode(phone, code);
    }

    // TODO redirect重定向是使用session来共享数据的，需要解决分布式session问题,redirectAttributes可以模拟重定向携带数据

    /**
     *
     * @param userRegisterVo
     * @param result JSR303参数校验结果
     * @return
     */
    @PostMapping("/register")
    public String register(@Validated UserRegisterVo userRegisterVo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            //tomap会报错java.lang.IllegalStateException: Duplicate key
//            Map<String, String> errorsMap = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            // 重定向跳转时携带一闪而过的数据，只会被使用一次
            Map<String, String> errors = new HashMap<>();

            result.getFieldErrors().forEach(item->{
                // 获取错误的属性名和错误信息
                errors.put(item.getField(), item.getDefaultMessage());
                //1.2 将错误信息封装到session中
                redirectAttributes.addFlashAttribute("errors", errors);
            });

            redirectAttributes.addFlashAttribute("errors", errors);
            // 重定向跳转，不使用转发的原因是/regist是post请求的，如果转发，也会post请求reg.html会出现方法不支持
            return "redirect:http://auth.mymall.com:1111/reg.html";
        }
        // 真正的注册，调用远程服务进行注册
        // 校验验证码是否有效
        String redisCodeValue = redisTemplate.opsForValue().get(AuthConst.SMS_REDIS_PREFIX_CONST + userRegisterVo.getPhone());
        if (!StringUtils.isEmpty(redisCodeValue)) {
            String code = redisCodeValue.split("_")[0];
            if (code.equalsIgnoreCase(userRegisterVo.getCode())) {
                // 验证码使用时候删除
                redisTemplate.delete(AuthConst.SMS_REDIS_PREFIX_CONST + userRegisterVo.getPhone());
                // 调用远程服务进行注册
                R register = myMallMemberFeignService.register(userRegisterVo);
                if (register.getCode() == 0) {
                    return "redirect:http://auth.mymall.com:1111/login.html";

                }else {
                    Map<String, String> errors = new HashMap<>();
                    errors.put("msg", (String) register.get("msg"));
                    redirectAttributes.addFlashAttribute("errors", errors);
                    return "redirect:http://auth.mymall.com:1111/reg.html";
                }
            }
        }
        Map<String, String> errors = new HashMap<>();
        errors.put("code", ExceptionEnum.SMS_CODE_VERIFY_FAILED.getDesc());
        redirectAttributes.addFlashAttribute("errors", errors);
        return "redirect:http://auth.mymall.com:1111/reg.html";

    }
}
