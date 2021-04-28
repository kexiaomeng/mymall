package com.tracy.mymall.order.dao;

import com.tracy.mymall.order.entity.MqMessageEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:31:12
 */
@Mapper
public interface MqMessageDao extends BaseMapper<MqMessageEntity> {
	
}
