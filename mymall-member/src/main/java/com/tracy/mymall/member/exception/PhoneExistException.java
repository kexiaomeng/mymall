package com.tracy.mymall.member.exception;

import com.tracy.mymall.common.exception.RRException;

public class PhoneExistException extends RRException {
    public PhoneExistException(int code, String msg) {
        super(code, msg);
    }
}
