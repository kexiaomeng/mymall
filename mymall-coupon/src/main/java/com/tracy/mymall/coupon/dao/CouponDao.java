package com.tracy.mymall.coupon.dao;

import com.tracy.mymall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-23 00:13:19
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
