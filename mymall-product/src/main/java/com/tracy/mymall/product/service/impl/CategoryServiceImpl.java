package com.tracy.mymall.product.service.impl;

import com.tracy.mymall.product.common.CategoryLevelEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.product.dao.CategoryDao;
import com.tracy.mymall.product.entity.CategoryEntity;
import com.tracy.mymall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listByTree() {
        // 1. 查出所有分类
        List<CategoryEntity> allCategories = baseMapper.selectList(null);
        // 2. 找出父子结构

        // 2.1 stream过滤出一层结构，category中添加下层列表结构，通过递归获取
        List<CategoryEntity> firstLevelProducts = allCategories.stream()
                .filter((entity) -> entity.getParentCid().equals(CategoryLevelEnum.DEFAULT.getNumber()))
                .map(currentCategory -> {
                    currentCategory.setCategories(getSubCategories(currentCategory, allCategories));
                    return currentCategory;
                })
                .sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        return firstLevelProducts;
    }

    /**
     * 自定义的批量删除方法，删除前会有其他一些动作
     * @param asList
     */
    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 删除前会有其他一些动作
        baseMapper.deleteBatchIds(asList);
    }

    /**
     * 递归获取层级结构,递归结束的条件是filter条件筛选不到数据
     * @param currentCategory
     * @param allCategories
     * @return
     */
    private List<CategoryEntity> getSubCategories(CategoryEntity currentCategory, List<CategoryEntity> allCategories) {

        List<CategoryEntity> subCategories = allCategories.stream()
                .filter(entity -> entity.getParentCid().equals(currentCategory.getCatId()))
                .map((entity) -> {
                    entity.setCategories(getSubCategories(entity, allCategories));
                    return entity;
                })
                .sorted((entity1, entity2) -> (entity1.getSort() == null ? 0 : entity1.getSort()) - (entity2.getSort() == null ? 0 : entity2.getSort()))
                .collect(Collectors.toList());
        return subCategories;
    }

}