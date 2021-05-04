package com.tracy.mymall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.dto.SkuReductionDto;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-23 00:13:18
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveRecudtion(SkuReductionDto skuReductionDto);
}

