package com.tracy.mymall.common.exception;

/**
 * 异常类定义 errorcode： 00:全局 000：未知
 * 11:商品服务 001参数校验异常
 */
public enum  ExceptionEnum {

    UNKNOW_EXCEPTION(10000, "全局未知异常"),
    UNKNOW_PRODUCT_EXCEPTION(11000, "商品服务未知异常"),
    VALID_PRODUCT_EXCEPTION(11001,"商品服务参数校验异常");

    private int errorCode;
    private String desc;

    private ExceptionEnum(int errorCode, String desc) {
        this.errorCode = errorCode;
        this.desc = desc;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getDesc() {
        return desc;
    }
}
