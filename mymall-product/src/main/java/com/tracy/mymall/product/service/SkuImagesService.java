package com.tracy.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.product.entity.SkuImagesEntity;

import java.util.List;
import java.util.Map;

/**
 * sku图片
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-21 21:10:37
 */
public interface SkuImagesService extends IService<SkuImagesEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuImagesEntity> queryBySkuId(Long skuId);
}

