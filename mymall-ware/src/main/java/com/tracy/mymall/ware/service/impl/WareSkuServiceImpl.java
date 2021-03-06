package com.tracy.mymall.ware.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tracy.mymall.common.dto.SkuHasStockDto;
import com.tracy.mymall.common.dto.mq.StockDetailTo;
import com.tracy.mymall.common.dto.mq.WareLockedDto;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.common.vo.WareLockVo;
import com.tracy.mymall.ware.entity.WareOrderTaskDetailEntity;
import com.tracy.mymall.ware.entity.WareOrderTaskEntity;
import com.tracy.mymall.ware.exception.NoStockException;
import com.tracy.mymall.ware.feign.ProductFeignService;
import com.tracy.mymall.ware.kafka.KafkaConsumer;
import com.tracy.mymall.ware.kafka.KafkaProducer;
import com.tracy.mymall.ware.service.WareOrderTaskDetailService;
import com.tracy.mymall.ware.service.WareOrderTaskService;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private WareOrderTaskDetailService detailService;

    @Autowired
    private WareOrderTaskService taskService;

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
            //??????skuname?????????
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
            // ??????sku???????????????????????????????????????????????????????????????????????????
            Integer count = this.baseMapper.selectStock(skuId);
            skuHasStockDto.setHasStock(count == null? false : count > 0);
            return skuHasStockDto;
        }).collect(Collectors.toList());

        return collect;
    }

    /**
     * // TODO ??????????????????id??????????????????????????????????????????
     * @param wareLockVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public boolean lockStock(WareLockVo wareLockVo) {
        // ?????????????????????

        //?????????????????????
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareLockVo.getOrderSn());
        taskService.save(taskEntity);


        // 1. ?????????????????????????????????????????????
        List<WareLockVo.WareOrderItem> orderItems = wareLockVo.getOrderItems();
        List<SkuStockInfo> skuStockInfos = orderItems.stream().map(item -> {
            SkuStockInfo skuStockInfo = new SkuStockInfo();
            skuStockInfo.setSkuId(item.getSkuId());
            skuStockInfo.setSkuOrderItemNum(item.getCount());
            List<Long> longs = this.baseMapper.queryWhichWareHasStockOfSku(item.getSkuId());
            skuStockInfo.setWareIds(longs);
            return skuStockInfo;
        }).collect(Collectors.toList());


        for (SkuStockInfo skuStockInfo : skuStockInfos) {
            Boolean hasStock = false;

            List<Long> wareIds = skuStockInfo.getWareIds();
            if (wareIds == null || wareIds.size() == 0) {
                throw new NoStockException(skuStockInfo.getSkuId() + "????????????");
            }
            // ??????????????????????????????
            for (Long wareId : wareIds) {
                Long aLong = this.baseMapper.subStockNumFromCertainWare(wareId, skuStockInfo.getSkuId(), skuStockInfo.getSkuOrderItemNum());
                // ?????????????????????????????????????????????????????????
                // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                if (aLong > 0) {
                    // ???????????????????????????
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                    detailEntity.setLockStatus(1);
                    detailEntity.setSkuId(skuStockInfo.getSkuId());
                    detailEntity.setSkuNum(skuStockInfo.getSkuOrderItemNum());
                    detailEntity.setTaskId(taskEntity.getId());
                    detailEntity.setWareId(wareId);
                    detailService.save(detailEntity);

                    // ?????????????????????
                    WareLockedDto wareLockedDto = new WareLockedDto();
                    wareLockedDto.setTaskId(taskEntity.getId());

                    StockDetailTo stockDetailTo = new StockDetailTo();
                    BeanUtils.copyProperties(detailEntity, stockDetailTo);
                    wareLockedDto.setStockDetailTo(stockDetailTo);
                    System.out.println(JSON.toJSONString(wareLockedDto));

                    kafkaProducer.send(KafkaConsumer.lockedTopic, wareLockedDto.getTaskId().toString(), wareLockedDto);

                    hasStock = true;
                    break;
                }
            }

            if (!hasStock) {
                throw new NoStockException(skuStockInfo.getSkuId() + "????????????");
            }

        }
        return true;


    }

    @Override
    public void unlockStock(Long skuId, Integer skuNum, Long wareId, Long detailId) {
        // ??????
        this.baseMapper.unlockStock( skuId,  skuNum,  wareId);
        // ??????????????????????????????????????????
        detailService.updateWareTaskDetailState(detailId, 2);
    }

    /**
     * ??????sku?????????????????????
     */
    @Data
    private class SkuStockInfo{
        private Long skuId;
        // ?????????????????????
        private Integer skuOrderItemNum;
        // ????????????????????????sku??????
        private List<Long> wareIds;
    }

}