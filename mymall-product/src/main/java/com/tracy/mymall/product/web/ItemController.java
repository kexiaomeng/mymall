package com.tracy.mymall.product.web;

import com.tracy.mymall.product.service.SkuInfoService;
import com.tracy.mymall.product.vo.SkuItemVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ItemController {
    @Autowired
    private SkuInfoService skuInfoService;

    @GetMapping("/{skuId}.html")
    public String skuItem(@PathVariable(value = "skuId") Long skuId, Model model) {
        System.out.println("开始查询" + skuId + " 的详细信息");
        SkuItemVo skuItemVo = skuInfoService.getSkuItem(skuId);
        model.addAttribute("item", skuItemVo);
        return "item";
    }
}
