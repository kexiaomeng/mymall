package com.tracy.mymall.thirdpart.exception;

import com.tracy.mymall.common.exception.RRException;
import com.tracy.mymall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
@ResponseBody
@Slf4j
public class MyMallThirdPartGlobalExceptionHandler {

    @ExceptionHandler(RRException.class)
    public R rrexceptionHandler(RRException exception) {
        log.error("mymall-third发生异常，[{}]", exception.getMsg());
        return R.error(exception.getCode(), exception.getMsg());
    }
}
