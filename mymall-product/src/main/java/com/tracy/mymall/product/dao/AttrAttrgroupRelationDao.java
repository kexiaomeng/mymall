package com.tracy.mymall.product.dao;

import com.tracy.mymall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {
    void deleteBatchRelations(@Param("collect") List<AttrAttrgroupRelationEntity> collect);

}
