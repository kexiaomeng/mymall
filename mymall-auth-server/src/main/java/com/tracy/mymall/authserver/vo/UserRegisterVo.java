package com.tracy.mymall.authserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterVo {

    @NotEmpty(message = "用户名不能为空")
    @Length(min = 4, max = 18, message = "用户名长度必须为4-18之间")
    private String userName;
    @NotEmpty(message = "密码不能为空")
    @Length(min = 4, max = 18, message = "密码长度必须为4-18之间")
    private String password;

    @NotEmpty(message = "手机号不能为空")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码不能为空")
    private String code;
}
