package com.tracy.mymall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.product.entity.CategoryEntity;
import com.tracy.mymall.product.vo.CateLogIndexVo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询层级结构
     * @return
     */
    List<CategoryEntity> listByTree();

    /**
     * 自定义的删除方法，还会有其他一些处理
     * @param asList
     */
    void removeMenuByIds(List<Long> asList);

    Long[] findCategoryPath(Long catelogId);

    void updateCascade(CategoryEntity categoryEntity);

    List<CategoryEntity> getFirsetLevelCategory();

    Map<String, List<CateLogIndexVo>> getCateLogIndexJson();
    public void updateCascadeConsistency(CategoryEntity categoryEntity);
    public Map<String, List<CateLogIndexVo>> getCateLogIndexJsonConcurrent();
}

