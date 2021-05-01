package com.tracy.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.product.entity.AttrEntity;
import com.tracy.mymall.product.vo.AttrRespVo;
import com.tracy.mymall.product.vo.AttrVo;

import java.util.Map;

/**
 * 商品属性
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
public interface AttrService extends IService<AttrEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveCascade(AttrVo attr);

    PageUtils queryBasePageByCategory(Map<String, Object> params, Long catelogId);

    AttrRespVo getAttrInfo(Long attrId);

    void updateAttrVo(AttrVo attr);
}

