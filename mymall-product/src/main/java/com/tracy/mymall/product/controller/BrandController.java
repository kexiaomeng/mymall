package com.tracy.mymall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.tracy.mymall.common.valid.AddGroup;
import com.tracy.mymall.common.valid.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tracy.mymall.product.entity.BrandEntity;
import com.tracy.mymall.product.service.BrandService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 00:34:22
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * 定义校验规则,注释后在全局统一异常处理部分了
     */
    @RequestMapping("/save")
    public R save(@Valid @RequestBody BrandEntity brand/*, BindingResult bindingResult*/){
       /*
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                String field = error.getField();
                String defaultMessage = error.getDefaultMessage();
                errorMap.put(field, defaultMessage);

            });
            return R.error(400, "数据校验异常").put("data", errorMap);
        }*/
		brandService.save(brand);

        return R.ok();
    }

    /**
     * 保存
     * 定义校验规则,注释后在全局统一异常处理部分了
     */
    @RequestMapping("/saveg")
    public R saveg(@Validated(AddGroup.class) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/){
       /*
        if (bindingResult.hasErrors()) {
            Map<String, String> errorMap = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> {
                String field = error.getField();
                String defaultMessage = error.getDefaultMessage();
                errorMap.put(field, defaultMessage);

            });
            return R.error(400, "数据校验异常").put("data", errorMap);
        }*/
        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@Validated(UpdateGroup.class)@RequestBody BrandEntity brand){
//		brandService.updateById(brand);
        //级联更新所有数据
        brandService.updateCascade(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
