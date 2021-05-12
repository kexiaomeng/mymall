package com.tracy.mymall.product.web;

import com.tracy.mymall.product.entity.CategoryEntity;
import com.tracy.mymall.product.service.CategoryService;
import com.tracy.mymall.product.vo.CateLogIndexVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class IndexController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 首页导航，默认展示一级菜单
     * @param model
     * @return
     */
    @GetMapping({"","index.html"})
    public String index(Model model) {
        List<CategoryEntity> categoryEntities = categoryService.getFirsetLevelCategory();
        model.addAttribute("categories",categoryEntities);
        return "index";
    }

    @GetMapping("/index/json/catalog.json")
    @ResponseBody
    public Map<String, List<CateLogIndexVo>> getCatelog() {
        Map<String, List<CateLogIndexVo>> result =  categoryService.getCateLogIndexJson();
        return result;
    }

}
