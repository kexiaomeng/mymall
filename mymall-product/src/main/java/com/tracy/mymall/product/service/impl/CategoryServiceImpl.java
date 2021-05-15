package com.tracy.mymall.product.service.impl;

import com.tracy.mymall.product.common.CategoryLevelEnum;
import com.tracy.mymall.product.service.CategoryBrandRelationService;
import com.tracy.mymall.product.vo.CateLogIndexVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.transaction.annotation.Transactional;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
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

    /**
     * 获取分类层级，例如[2,5,255] /数码/手机/品牌
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> categoryPathList = new ArrayList<>();
        categoryPathList.add(catelogId);
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        // while循环遍历查询
        while (categoryEntity != null && categoryEntity.getParentCid() != 0) {
            categoryPathList.add(categoryEntity.getParentCid());
            categoryEntity = baseMapper.selectById(categoryEntity.getParentCid());

        }

        Collections.reverse(categoryPathList);

        return categoryPathList.toArray(new Long[categoryPathList.size()]);
    }

    /**
     * 级联更新
     * @param categoryEntity
     */
    @Override
    @Transactional
    public void updateCascade(CategoryEntity categoryEntity) {
        baseMapper.updateById(categoryEntity);
        if (StringUtils.isNotEmpty(categoryEntity.getName())) {
            categoryBrandRelationService.updateCategoryName(categoryEntity.getCatId(), categoryEntity.getName());
        }
    }

    @Override
    public List<CategoryEntity> getFirsetLevelCategory() {
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", CategoryLevelEnum.DEFAULT.getNumber()));
        return categoryEntities;
    }

    @Override
    public Map<String, List<CateLogIndexVo>> getCateLogIndexJson() {
        // 查询出所有的记录
        List<CategoryEntity> categoryEntities = this.baseMapper.selectList(null);

        // 1. 查询一级分类
        List<CategoryEntity> firsetLevelCategory = this.getCategoriesByParentId(categoryEntities, 0L);
        Map<String, List<CateLogIndexVo>> collect = firsetLevelCategory
                .stream()
                // 将数据收集成map
                .collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                    // 根据一级分类查询二级分类
                    List<CategoryEntity> secondLevel = this.getCategoriesByParentId(categoryEntities, v.getCatId());
                    List<CateLogIndexVo> cateLogIndexVos = new ArrayList<>();
                    if (secondLevel != null) {
                        // 根据二级分类查询三级分类
                        cateLogIndexVos = secondLevel.stream().map(item -> {
                            CateLogIndexVo cateLogIndexVo = new CateLogIndexVo();
                            cateLogIndexVo.setCatalog1Id(v.getCatId());
                            cateLogIndexVo.setId(item.getCatId());
                            cateLogIndexVo.setName(item.getName());
                            List<CategoryEntity> thirdLevel =  this.getCategoriesByParentId(categoryEntities, item.getCatId());
                            List<CateLogIndexVo.CateLog3Vo> cateLog3Vos = new ArrayList<>();
                            if (thirdLevel != null && !thirdLevel.isEmpty()) {
                                // 组装3级分类信息
                                cateLog3Vos = thirdLevel.stream().map(level -> {
                                    CateLogIndexVo.CateLog3Vo cateLog3Vo = new CateLogIndexVo.CateLog3Vo();
                                    cateLog3Vo.setCatalog2Id(item.getCatId());
                                    cateLog3Vo.setId(level.getCatId());
                                    cateLog3Vo.setName(level.getName());
                                    return cateLog3Vo;
                                }).collect(Collectors.toList());
                            }
                            cateLogIndexVo.setCatalog3List(cateLog3Vos);
                            return cateLogIndexVo;
                        }).collect(Collectors.toList());
                    }
                    return cateLogIndexVos;

            }));
            return collect;
    }

    public List<CategoryEntity> getCategoriesByParentId(List<CategoryEntity> allCategories, Long parentId) {
        List<CategoryEntity> collect = allCategories.stream().filter(item -> item.getParentCid().equals(parentId)).collect(Collectors.toList());
        return collect;
    }

}