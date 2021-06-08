package com.tracy.mymall.member.exception;

import com.tracy.mymall.common.exception.RRException;

public class UserExistException extends RRException {
    public UserExistException(int code, String msg) {
        super(code, msg);
    }
}