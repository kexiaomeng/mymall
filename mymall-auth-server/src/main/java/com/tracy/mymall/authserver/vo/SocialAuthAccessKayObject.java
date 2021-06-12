package com.tracy.mymall.authserver.vo;

import lombok.Data;

@Data
public class SocialAuthAccessKayObject {
    private String client_id="2635041892";
    private String client_secret="4b75226f62784255535bd606547c22c1";
    private String grant_type="authorization_code";
    private String redirect_uri="http://auth.mymall.com:1111/oauth/weibo/success";
    private String code;
}
