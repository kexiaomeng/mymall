package com.tracy.mymall.authserver.vo;

import lombok.Data;

/**
 * 第三方登录获取accesskey的返回结果
 */
@Data
public class SocialRespVo {
      private String access_token;
      private String remind_in;
      private String expires_in;
      private String uid;
      private String isRealName;
}
