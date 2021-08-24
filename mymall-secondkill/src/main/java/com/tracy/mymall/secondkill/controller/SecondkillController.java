package com.tracy.mymall.secondkill.controller;

import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.secondkill.dto.SeckillSkuRedisTo;
import com.tracy.mymall.secondkill.service.SecondkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端通过异步请求获取秒杀的商品信息
 */
@Controller
public class SecondkillController {
    @Autowired
    private SecondkillService secondkillService;

    /**
     * 获取当前时间段可以秒杀的商品
     * @return
     */
    @GetMapping("/getCurrentSeckillSkus")
    @ResponseBody
    public R getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> seckillSkuRedisTos = secondkillService.getCurrentSeckillSkus();
        return R.ok().put("data", seckillSkuRedisTos);
    }

    @GetMapping("/seckill/sku/{skuId}")
    @ResponseBody
    public R getSkuSecondkillMsg(@PathVariable("skuId")Long skuId) {
        SeckillSkuRedisTo to = secondkillService.getSkuSecondkillMsg(skuId);
        if (to != null) {
            return R.ok().put("data", to);
        }else {
            return R.error(2222,"当前商品没有找到秒杀信息");
        }

    }

    @GetMapping("/kill")
    public String seckill(@RequestParam("killId") Long killId, @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {
        String orderSn = secondkillService.seckill(killId, key, num);
        model.addAttribute("orderSn", orderSn);

        return "success";
    }

}
