package com.tracy.mymall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.exception.RRException;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.member.dto.UserRegisterDto;
import com.tracy.mymall.member.entity.MemberEntity;
import com.tracy.mymall.member.exception.PhoneExistException;
import com.tracy.mymall.member.exception.UserExistException;

import java.util.Map;

/**
 * 会员
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:23:53
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterDto userRegisterDto) throws RRException;
    void checkUniqueUserName(String userName) throws UserExistException;
    void checkUniquePhone(String phone) throws PhoneExistException;
}

