package com.tracy.mymall.secondkill.scheduled;

import com.tracy.mymall.secondkill.service.SecondkillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SecondkillSkuScheduled {
    @Autowired
    private SecondkillService secondkillService;

    /**
     * 每日凌晨3点上架未来3天需要秒杀的商品到缓存中，涉及到远程查询优惠服务
     * 需要确保幂等性，确保分布式环境下数据只能被一个节点进行处理,保证数据不会重复
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void uploadSecondkillskuLatest3Days() {
        secondkillService.uploadSeckillSkuLatest3Day();
    }
}
