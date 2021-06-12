package com.tracy.mymall.member.service.impl;

import com.tracy.mymall.common.exception.ExceptionEnum;
import com.tracy.mymall.common.exception.RRException;
import com.tracy.mymall.member.dto.MemberLoginDto;
import com.tracy.mymall.member.dto.SocialUserVo;
import com.tracy.mymall.member.dto.UserRegisterDto;
import com.tracy.mymall.member.entity.MemberLevelEntity;
import com.tracy.mymall.member.exception.PhoneExistException;
import com.tracy.mymall.member.exception.UserExistException;
import com.tracy.mymall.member.service.MemberLevelService;
import com.tracy.mymall.member.vo.WeiboUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Member;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.member.dao.MemberDao;
import com.tracy.mymall.member.entity.MemberEntity;
import com.tracy.mymall.member.service.MemberService;
import org.springframework.web.client.RestTemplate;


@Service("memberService")
@Slf4j
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;
    @Autowired
    private RestTemplate restTemplate;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<>()
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
        memberEntity.setNickname(userRegisterDto.getUserName());

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

    @Override
    public MemberEntity login(MemberLoginDto memberLoginDto) {
        // 1. 检查是否存在用户,根据用户名或者手机号


        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().eq("username", memberLoginDto.getLoginAccount()).or().eq("mobile", memberLoginDto.getLoginAccount()));

        if (memberEntity == null) {
            return null;
        }

        // 2. 检查密码是否正确
        String md5Password = memberEntity.getPassword();
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(memberLoginDto.getPassword(), md5Password);
        if (matches) {
            memberEntity.setPassword(null);
            return memberEntity;
        }
        return null;
    }

    @Override
    public MemberEntity socialLogin(SocialUserVo socialUserVo) {
        String accessToken = socialUserVo.getAccess_token();
        String uid = socialUserVo.getUid();
        String expiresIn =  socialUserVo.getExpires_in();
        // 1.查询用户是否存在，如果存在，则直接更新accesskey并返回相关信息，否则进行注册，注册的时候把第三方的昵称和头像都查询出来
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", uid));
        if (memberEntity != null) {
            memberEntity.setId(memberEntity.getId());
            memberEntity.setAccessToken(accessToken);
            memberEntity.setSocialUid(uid);
            memberEntity.setExpiresIn(expiresIn);
            this.baseMapper.updateById(memberEntity);
            return memberEntity;
        } else {
            memberEntity = new MemberEntity();
            memberEntity.setAccessToken(accessToken);
            memberEntity.setSocialUid(uid);
            memberEntity.setExpiresIn(expiresIn);
            //会员id设置为默认
            MemberLevelEntity memberLevelEntity = memberLevelService.getDefaultMemberLevel();
            memberEntity.setLevelId(0L);
            if (memberLevelEntity != null) {
                memberEntity.setLevelId(memberLevelEntity.getId());
            }
            try {
                // 远程查询用户的头像等信息
                // 2. 没有查到当前社交用户对应的记录 我们就需要注册一个
                HashMap<String, String> map = new HashMap<>();
                map.put("access_token", accessToken);
                map.put("uid", uid);

                ResponseEntity<WeiboUserVo> entity = restTemplate.getForEntity("https://api.weibo.com/2/users/show.json?access_token={access_token}&uid={uid}", WeiboUserVo.class, map);
                if (entity.getStatusCode() == HttpStatus.OK) {
                    WeiboUserVo userVo = entity.getBody();
                    memberEntity.setNickname(userVo.getName());
                    memberEntity.setUsername(userVo.getName());
                    memberEntity.setGender("m".equals(userVo.getGender()) ? 1 : 0 );
                    memberEntity.setCity(userVo.getLocation());
                    memberEntity.setJob("自媒体");
                }
            }catch(Exception e) {
                log.error("", e);
            }

            memberEntity.setStatus(0);
            memberEntity.setCreateTime(new Date());
            memberEntity.setBirth(new Date());
            memberEntity.setPassword(null);

            this.baseMapper.insert(memberEntity);
            log.info("【会员】用户[{}]注册成功", memberEntity.getUsername());
        }



        return memberEntity;
    }

}