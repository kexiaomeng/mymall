package com.tracy.mymall.ware.dao;

import com.tracy.mymall.ware.entity.PurchaseEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购信息
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:29:49
 */
@Mapper
public interface PurchaseDao extends BaseMapper<PurchaseEntity> {
	
}
