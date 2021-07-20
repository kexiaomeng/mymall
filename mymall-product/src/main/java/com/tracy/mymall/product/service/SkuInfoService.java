package com.tracy.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.product.entity.SkuInfoEntity;
import com.tracy.mymall.product.entity.SpuInfoEntity;
import com.tracy.mymall.product.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-21 21:10:37
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);
    PageUtils queryPageByCondition(Map<String, Object> params);

    /**
     * 根据spuid获取sku信息
     * @return
     */
    List<SkuInfoEntity> getSkuBySpuId(Long spuId);

    SkuItemVo getSkuItem(Long skuId);

    SpuInfoEntity getSpuBySkuId(Long skuId);
}

