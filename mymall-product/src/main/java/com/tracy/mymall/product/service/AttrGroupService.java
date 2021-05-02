package com.tracy.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.product.entity.AttrEntity;
import com.tracy.mymall.product.entity.AttrGroupEntity;
import com.tracy.mymall.product.vo.AttrAttrgroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, long catelogId);

    List<AttrEntity> getAttrGroupsAttr(Map<String, Object> params, Long attrgroupId);

    void batchDeleteRelation(List<AttrAttrgroupVo> asList);

    PageUtils getAttrGroupsAttrNoRelation(Map<String, Object> params, Long attrgroupId);

    void addRelation(List<AttrAttrgroupVo> attrAttrgroupVos);
}

