package com.tracy.mymall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;
import com.tracy.mymall.product.dao.BrandDao;
import com.tracy.mymall.product.dao.CategoryDao;
import com.tracy.mymall.product.entity.BrandEntity;
import com.tracy.mymall.product.entity.CategoryEntity;
import com.tracy.mymall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.tracy.mymall.product.dao.CategoryBrandRelationDao;
import com.tracy.mymall.product.entity.CategoryBrandRelationEntity;
import com.tracy.mymall.product.service.CategoryBrandRelationService;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {

    @Autowired
    BrandDao brandDao;
    @Autowired
    CategoryDao categoryDao;
    @Autowired
    private BrandService brandService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> queryCategoryByBrandId(Long brandId) {
        List<CategoryBrandRelationEntity> data = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("brand_id", brandId));
        return data;
    }

    @Override
    public void saveDetail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();

        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);

        categoryBrandRelation.setBrandName(brandEntity.getName());
        categoryBrandRelation.setCatelogName(categoryEntity.getName());

        this.save(categoryBrandRelation);
    }

    /**
     * 更新品牌名称的时候更新关联表的名称
     * @param brandId
     * @param name
     */
    @Override
    public void updateBrandName(Long brandId, String name) {
        this.baseMapper.updateBrand(brandId, name);
    }
    /**
     * 更新类别名称的时候更新关联表的名称
     * @param catId
     * @param name
     */
    @Override
    public void updateCategoryName(Long catId, String name) {
        CategoryBrandRelationEntity entity = new CategoryBrandRelationEntity();
        entity.setCatelogId(catId);
        entity.setCatelogName(name);

        UpdateWrapper<CategoryBrandRelationEntity> updateWrapper = new UpdateWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId);
        this.update(entity, updateWrapper);
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> brandsRelations = this.list(new QueryWrapper<CategoryBrandRelationEntity>().eq("catelog_id", catId));
        List<Long> brandIds = brandsRelations.stream().map(CategoryBrandRelationEntity::getBrandId).collect(Collectors.toList());
        if (!brandIds.isEmpty()) {
            Collection<BrandEntity> brandEntities = brandService.listByIds(brandIds);
            return (List<BrandEntity>) brandEntities;
        }
        return new ArrayList<>();

    }

}