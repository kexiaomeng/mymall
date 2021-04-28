package com.tracy.mymall.product.dao;

import com.tracy.mymall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
