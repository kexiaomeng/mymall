package com.tracy.mymall.thirdpart.controller;

import com.tracy.mymall.common.exception.ExceptionEnum;
import com.tracy.mymall.common.exception.RRException;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.thirdpart.component.SmsSenderComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短信服务
 */
@RestController
@Slf4j
public class SmsController {

    @Autowired
    private SmsSenderComponent smsSenderComponent;
    /**
     * 后台的各种服务调用第三发服务，而不是页面直接调用
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/sms/sendCode")
    public R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code) throws RRException {
        try {
            smsSenderComponent.sendSms(phone, code);
            return R.ok();
        } catch (Exception e) {
            throw new RRException(ExceptionEnum.SMS_CODE_SEND_FAILED.getErrorCode(),ExceptionEnum.SMS_CODE_SEND_FAILED.getDesc());
        }

    }
}
