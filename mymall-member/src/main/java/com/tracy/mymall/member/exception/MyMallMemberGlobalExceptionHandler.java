package com.tracy.mymall.member.exception;

import com.tracy.mymall.common.exception.ExceptionEnum;
import com.tracy.mymall.common.exception.RRException;
import com.tracy.mymall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常统一处理
 */
@RestControllerAdvice
@ResponseBody
@Slf4j
public class MyMallMemberGlobalExceptionHandler {


    @ExceptionHandler(RRException.class)
    public R rrexceptionHandler(RRException exception) {
        log.error("mymall-member发生异常，[{}]", exception.getMsg());
        return R.error(exception.getCode(), exception.getMsg());
    }

    @ExceptionHandler(Exception.class)
    public R exceptionHandler(Exception exception) {
        log.error("mymall-member发生异常，[{}]", exception.getMessage());
        return R.error( exception.getMessage());
    }



}
