package com.tracy.mymall.ware.dao;

import com.tracy.mymall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * εεεΊε­
 * 
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:29:48
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("skuNum") Integer skuNum);

    Integer selectStock(@Param("skuId") Long skuId);

    List<Long> queryWhichWareHasStockOfSku(@Param("skuId") Long skuId);

    Long subStockNumFromCertainWare(@Param("wareId") Long wareId,@Param("skuId") Long skuId, @Param("num")Integer num);

    void unlockStock(@Param("skuId") Long skuId, @Param("skuNum") Integer skuNum, @Param("wareId") Long wareId);
}
