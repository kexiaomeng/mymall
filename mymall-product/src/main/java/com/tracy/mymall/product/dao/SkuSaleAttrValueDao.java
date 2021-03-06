package com.tracy.mymall.product.dao;

import com.tracy.mymall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tracy.mymall.product.vo.SkuItemAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-21 21:10:37
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuItemAttrVo> getAllSkuSaleAttrBySpuId(@Param("spuId") long spuId);

    List<String> getSkuSaleAttrStringBySkuId(@Param("skuId") Long skuId);
}
