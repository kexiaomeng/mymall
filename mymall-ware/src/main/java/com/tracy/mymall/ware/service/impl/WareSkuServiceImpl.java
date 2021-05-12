package com.tracy.mymall.ware.service.impl;

import com.tracy.mymall.common.dto.SkuHasStockDto;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.ware.feign.ProductFeignService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tracy.mymall.common.utils.PageUtils;
import com.tracy.mymall.common.utils.Query;

import com.tracy.mymall.ware.dao.WareSkuDao;
import com.tracy.mymall.ware.entity.WareSkuEntity;
import com.tracy.mymall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    ProductFeignService productFeignService;
    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.eq("ware_id", wareId);
        }

        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId) ) {
            queryWrapper.and( query -> {
                query.eq("sku_id", skuId);
            });
        }



        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ware_id", wareId).eq("sku_id", skuId);
        Integer count = this.baseMapper.selectCount(queryWrapper);
        if (count == 0) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();

            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            //查出skuname并设置
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> data = (Map<String, Object>) info.get("skuInfo");
                wareSkuEntity.setSkuName((String) data.get("skuName"));
            } catch (Exception e) {
            }
            this.baseMapper.insert(wareSkuEntity);
        } else {
            this.baseMapper.addStock(skuId, wareId, skuNum);
        }
    }

    @Override
    public List<SkuHasStockDto> hasStock(List<Long> skuIds) {
        List<SkuHasStockDto> collect = skuIds.stream().map(skuId -> {
            SkuHasStockDto skuHasStockDto = new SkuHasStockDto();
            skuHasStockDto.setSkuId(skuId);
            // 每个sku可能存在于不同的仓库当中，需要检索所有仓库中的数量
            Integer count = this.baseMapper.selectStock(skuId);
            skuHasStockDto.setHasStock(count == null? false : count > 0);
            return skuHasStockDto;
        }).collect(Collectors.toList());

        return collect;
    }

}