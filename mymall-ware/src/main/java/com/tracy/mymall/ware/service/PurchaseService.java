package com.tracy.mymall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.ware.entity.PurchaseEntity;
import com.tracy.mymall.ware.vo.MergeVo;
import com.tracy.mymall.ware.vo.PurchaseFinishVo;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:29:49
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils unreceiveList(Map<String, Object> params);

    void merge(MergeVo mergeVo);

    void receivePurchaseItem(List<Long> purchaseItems);

    void finishPurchase(PurchaseFinishVo purchaseFinishVo);

}

