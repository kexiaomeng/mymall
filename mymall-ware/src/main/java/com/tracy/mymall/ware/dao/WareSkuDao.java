package com.tracy.mymall.ware.dao;

import com.tracy.mymall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品库存
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:29:48
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {
	
}
