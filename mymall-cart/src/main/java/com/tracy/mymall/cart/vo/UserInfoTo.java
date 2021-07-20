package com.tracy.mymall.cart.vo;

import lombok.Data;

@Data
public class UserInfoTo {
    /**
     * 登录用户的id
     */
    private Long userId;

    /**
     * 临时用户的id
     */
    private String userKey;
    /**
     * 是否有临时用户的key
     */
    private Boolean tempUser = false;
}
