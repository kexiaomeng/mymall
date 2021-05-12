package com.tracy.mymall.ware.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.tracy.mymall.ware.vo.MergeVo;
import com.tracy.mymall.ware.vo.PurchaseFinishVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tracy.mymall.ware.entity.PurchaseEntity;
import com.tracy.mymall.ware.service.PurchaseService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.R;



/**
 * 采购信息
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:29:49
 */
@RestController
@RequestMapping("ware/purchase")
public class PurchaseController {
    @Autowired
    private PurchaseService purchaseService;


    /**
     * /ware/purchase/done
     * 领取采购单
     */
    @PostMapping("/done")
    public R done(@RequestBody PurchaseFinishVo purchaseFinishVo) {
        purchaseService.finishPurchase(purchaseFinishVo);
        return R.ok();
    }


    /**
     * /ware/purchase/received
     * 领取采购单
     */
    @PostMapping("/received")
    public  R receivePurchaseItem(@RequestBody List<Long> purchaseItems) {
        purchaseService.receivePurchaseItem(purchaseItems);
        return R.ok();
    }
    /**
     * 查询处于新建和分配状态的数据
     */
    @RequestMapping("/unreceive/list")
    public R unreceiveList(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.unreceiveList(params);

        return R.ok().put("page", page);
    }

    /**
     * 合并进货单
     */
    @PostMapping("/merge")
    public R merge(@RequestBody MergeVo mergeVo){
        purchaseService.merge(mergeVo);

        return R.ok();
    }
    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = purchaseService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id){
		PurchaseEntity purchase = purchaseService.getById(id);

        return R.ok().put("purchase", purchase);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody PurchaseEntity purchase){
		purchaseService.save(purchase);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody PurchaseEntity purchase){
		purchaseService.updateById(purchase);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids){
		purchaseService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
