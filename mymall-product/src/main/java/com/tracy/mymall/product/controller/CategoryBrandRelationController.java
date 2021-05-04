package com.tracy.mymall.product.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.tracy.mymall.product.entity.BrandEntity;
import com.tracy.mymall.product.vo.BrandRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracy.mymall.product.entity.CategoryBrandRelationEntity;
import com.tracy.mymall.product.service.CategoryBrandRelationService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.R;



/**
 * 品牌分类关联
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-21 22:00:43
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;


    /**
     * 根据分类id查询品牌
     * /product/categorybrandrelation/brands/list
     */
    @GetMapping("/brands/list")
    public R brandList(@RequestParam Long catId){
        List<BrandEntity> brandEntities = categoryBrandRelationService.getBrandsByCatId(catId);
        List<BrandRespVo> data = brandEntities.stream().map(item -> {
            BrandRespVo brandRespVo = new BrandRespVo();
            brandRespVo.setBrandId(item.getBrandId());
            brandRespVo.setBrandName(item.getName());
            return brandRespVo;
        }).collect(Collectors.toList());
        return R.ok().put("data", data);
    }
    /**
     * 查询根据品牌ID分类品牌关联信息列表
     */
    @GetMapping("/catelog/list")
    public R catelogList(@RequestParam Long brandId){
        List<CategoryBrandRelationEntity> data = categoryBrandRelationService.queryCategoryByBrandId(brandId);
        return R.ok().put("data", data);
    }


    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存，保存时需要重新去数据库查询品牌和分类名,在修改分类和品牌名称的时候也要更新关联关系表的数据
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
//		categoryBrandRelationService.save(categoryBrandRelation);
        categoryBrandRelationService.saveDetail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation){
		categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
