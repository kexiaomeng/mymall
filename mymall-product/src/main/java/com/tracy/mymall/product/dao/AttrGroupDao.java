package com.tracy.mymall.product.dao;

import com.tracy.mymall.product.entity.AttrAttrgroupRelationEntity;
import com.tracy.mymall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

}
