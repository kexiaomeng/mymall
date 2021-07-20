package com.tracy.mymall.common.exception;

/**
 * 异常类定义 errorcode： 00:全局 000：未知
 * 11:商品服务 001参数校验异常
 */
public enum  ExceptionEnum {
    /**
     * 10: 全局
     * 11：商品
     * 12:会员
     * 19：认证服务
     * 20: 库存服务
     */
    UNKNOW_EXCEPTION(10000, "全局未知异常"),
    UNKNOW_PRODUCT_EXCEPTION(11000, "商品服务未知异常"),
    VALID_PRODUCT_EXCEPTION(11001,"商品服务参数校验异常"),
    PRODUCT_ES_STATUS_UP(11002, "商品信息上架保存到es中异常"),
    SMS_CODE_SEND_FAILED(19001,"验证码发送失败"),
    SMS_CODE_GET_FREQUENTLY(19002,"获取验证码太频繁,请稍后再试"),
    SMS_CODE_VERIFY_FAILED(19003,"验证码验证失败"),
    MEMBER_USERNAME_EXIST(12001, "用户名已存在"),
    MEMBER_PHONE_EXIST(12002,"手机号已存在"),
    MEMBER_LOGIN_ERROR(12003,"用户名或密码错误"),
    NO_STOCK_EXCEPTION(20001, "商品库存不足") ;
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
