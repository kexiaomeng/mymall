package com.tracy.mymall.product.exception;

import com.tracy.mymall.common.exception.ExceptionEnum;
import com.tracy.mymall.common.utils.R;
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
public class MyMallProductGlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R exceptionHandler(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error -> {
                String field = error.getField();
                String defaultMessage = error.getDefaultMessage();
                errorMap.put(field, defaultMessage);

            });
        }

        return R.error(ExceptionEnum.VALID_PRODUCT_EXCEPTION.getErrorCode(), ExceptionEnum.VALID_PRODUCT_EXCEPTION.getDesc()).put("data", errorMap);
    }


//    @ExceptionHandler(Exception.class)
//    public R unknowExceptionHandler(Throwable throwable) {
//        return R.error(ExceptionEnum.UNKNOW_PRODUCT_EXCEPTION.getErrorCode(), ExceptionEnum.UNKNOW_PRODUCT_EXCEPTION.getDesc()).put("data", throwable.getMessage());
//    }
}
