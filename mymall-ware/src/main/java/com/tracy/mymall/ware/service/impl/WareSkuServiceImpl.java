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

    /**
     * // TODO 根据收货地址id获取最近的仓库进行锁库存操作
     * @param wareLockVo
     * @return
     */
    @Override
    @Transactional(rollbackFor = NoStockException.class)
    public boolean lockStock(WareLockVo wareLockVo) {
        // 锁定库存的操作

        //保存锁订单任务
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(wareLockVo.getOrderSn());
        taskService.save(taskEntity);


        // 1. 先查出传过来的订单项的所有库存
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
                throw new NoStockException(skuStockInfo.getSkuId() + "库存不足");
            }
            // 依次从仓库中扣减库存
            for (Long wareId : wareIds) {
                Long aLong = this.baseMapper.subStockNumFromCertainWare(wareId, skuStockInfo.getSkuId(), skuStockInfo.getSkuOrderItemNum());
                // 如果锁库存成功，则向延时队列中发送消息
                // 如果扣减库存不成功，则当前方法中的所有数据会回退，表中没有相应的数据，如果扣减库存成功，则当前方法中的数据库操作均会成功，
                if (aLong > 0) {
                    // 订单项扣减保存明细
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                    detailEntity.setLockStatus(1);
                    detailEntity.setSkuId(skuStockInfo.getSkuId());
                    detailEntity.setSkuNum(skuStockInfo.getSkuOrderItemNum());
                    detailEntity.setTaskId(taskEntity.getId());
                    detailEntity.setWareId(wareId);
                    detailService.save(detailEntity);

                    // 发送到延时队列
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
                throw new NoStockException(skuStockInfo.getSkuId() + "库存不足");
            }

        }
        return true;


    }

    @Override
    public void unlockStock(Long skuId, Integer skuNum, Long wareId, Long detailId) {
        // 解锁
        this.baseMapper.unlockStock( skuId,  skuNum,  wareId);
        // 更新库存工作单的状态为已解锁
        detailService.updateWareTaskDetailState(detailId, 2);
    }

    /**
     * 每个sku商品的库存信息
     */
    @Data
    private class SkuStockInfo{
        private Long skuId;
        // 需要锁定的数量
        private Integer skuOrderItemNum;
        // 哪些仓库里有当前sku商品
        private List<Long> wareIds;
    }

}