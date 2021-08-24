package com.tracy.mymall.secondkill.service;

import com.tracy.mymall.secondkill.dto.SeckillSkuRedisTo;

import java.util.List;

public interface SecondkillService {
    void uploadSeckillSkuLatest3Day();

    List<SeckillSkuRedisTo> getCurrentSeckillSkus();

    SeckillSkuRedisTo getSkuSecondkillMsg(Long skuId);

    /**
     *
     * @param killId 秒杀的sku relation表的id
     * @param key
     * @param num
     * @return 秒杀订单的订单号
     */
    String seckill(Long killId, String key, Integer num);
}
