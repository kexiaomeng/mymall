package com.tracy.mymall.member.service.impl;

import com.tracy.mymall.common.exception.ExceptionEnum;
import com.tracy.mymall.common.exception.RRException;
import com.tracy.mymall.member.dto.UserRegisterDto;
import com.tracy.mymall.member.entity.MemberLevelEntity;
import com.tracy.mymall.member.exception.PhoneExistException;
import com.tracy.mymall.member.exception.UserExistException;
import com.tracy.mymall.member.service.MemberLevelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.member.dao.MemberDao;
import com.tracy.mymall.member.entity.MemberEntity;
import com.tracy.mymall.member.service.MemberService;


@Service("memberService")
@Slf4j
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 注册用户，验证失败可以进行全局异常处理，直接抛出异常
     * @param userRegisterDto
     */
    @Override
    public void register(UserRegisterDto userRegisterDto) throws RRException {
        MemberEntity memberEntity = new MemberEntity();
        // 验证手机号是否被注册
        checkUniquePhone(userRegisterDto.getPhone());
        memberEntity.setMobile(userRegisterDto.getPhone());
        // 验证用户名是否被注册
        checkUniqueUserName(userRegisterDto.getPhone());

        memberEntity.setUsername(userRegisterDto.getUserName());
        //会员id设置为默认
        MemberLevelEntity memberLevelEntity = memberLevelService.getDefaultMemberLevel();
        memberEntity.setLevelId(0L);
        if (memberLevelEntity != null) {
            memberEntity.setLevelId(memberLevelEntity.getId());
        }

        // 密码需要加密存储，使用md5hash加盐的方式
        //BCryptPasswordEncoder使用sha-256+随机盐+密钥的方式进行加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String md5Password = passwordEncoder.encode(userRegisterDto.getPassword());
        memberEntity.setPassword(md5Password);

        this.baseMapper.insert(memberEntity);
        log.info("【会员】用户[{}]注册成功", memberEntity.getUsername());

    }

    @Override
    public void checkUniqueUserName(String userName) throws UserExistException {
        Integer integer = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", userName));
        if (integer > 0) {
            throw new UserExistException(ExceptionEnum.MEMBER_USERNAME_EXIST.getErrorCode(), ExceptionEnum.MEMBER_USERNAME_EXIST.getDesc());
        }
    }
    @Override
    public void checkUniquePhone(String phone) throws PhoneExistException {
        Integer integer = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (integer > 0) {
            throw new PhoneExistException(ExceptionEnum.MEMBER_PHONE_EXIST.getErrorCode(), ExceptionEnum.MEMBER_PHONE_EXIST.getDesc());
        }
    }

}