package com.tracy.mymall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tracy.mymall.product.entity.AttrEntity;
import com.tracy.mymall.product.service.CategoryService;
import com.tracy.mymall.product.vo.AttrAttrgroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracy.mymall.product.entity.AttrGroupEntity;
import com.tracy.mymall.product.service.AttrGroupService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.R;



/**
 * 属性分组
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;


    /**
     * 添加分组/product/attrgroup/attr/relation
     */
    @PostMapping("/attr/relation")
    public R addRelation(@RequestBody List<AttrAttrgroupVo> attrAttrgroupVos) {
        attrGroupService.addRelation(attrAttrgroupVos);
        return R.ok();
    }

    /**
     * 查询指定分组下所有的属性/product/attrgroup/{attrgroupId}/attr/relation
     * @param params
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R getAttrGroupsAttr(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId")Long attrgroupId){
        List<AttrEntity> attrEntities = attrGroupService.getAttrGroupsAttr(params, attrgroupId);

        return R.ok().put("data", attrEntities);
    }

    /**
     * 获取属性分组里面还没有关联的本分类里面的其他基本属性，方便添加新的关联
     * product/attrgroup/{attrgroupId}/noattr/relation
     * @param params
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getAttrGroupsAttrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId")Long attrgroupId){
        PageUtils pageUtils = attrGroupService.getAttrGroupsAttrNoRelation(params, attrgroupId);

        return R.ok().put("page", pageUtils);
    }

    /**
     * 删除属性和分组的关联关系/product/attrgroup/attr/relation/delete
     */

    @PostMapping("/attr/relation/delete")
    public R deleteRelation(@RequestBody List<AttrAttrgroupVo> attrAttrgroupVos){
        attrGroupService.batchDeleteRelation(attrAttrgroupVos);

        return R.ok();
    }
    /**
     * 列表
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId")long catelogId){
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        Long[] categoryPath = categoryService.findCategoryPath(attrGroup.getCatelogId());
        attrGroup.setCategoryPath(categoryPath);
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}
