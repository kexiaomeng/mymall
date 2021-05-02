package com.tracy.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.product.entity.AttrAttrgroupRelationEntity;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    AttrAttrgroupRelationEntity queryByAttrId(Long attrId);

    void deleteBatchRelations(List<AttrAttrgroupRelationEntity> collect);
}

