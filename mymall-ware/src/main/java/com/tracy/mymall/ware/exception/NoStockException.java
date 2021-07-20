package com.tracy.mymall.ware.exception;

import lombok.Data;

@Data
public class NoStockException extends RuntimeException{
    private String msg;
    public NoStockException(String msg) {
        this.msg = msg;
    }
}
