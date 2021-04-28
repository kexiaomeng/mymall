package com.tracy.mymall.member.dao;

import com.tracy.mymall.member.entity.MemberLevelEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员等级
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:23:53
 */
@Mapper
public interface MemberLevelDao extends BaseMapper<MemberLevelEntity> {
	
}
