package com.tracy.mymall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tracy.mymall.common.dto.SkuHasStockDto;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.vo.WareLockVo;
import com.tracy.mymall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author kexiaomeng
 * @email kexiaomeng@foxmail.com
 * @date 2021-04-22 01:29:48
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockDto> hasStock(List<Long> skuIds);

    boolean lockStock(WareLockVo wareLockVo);

    void unlockStock(Long skuId, Integer skuNum, Long wareId, Long detailId);
}

