package com.tracy.mymall.secondkill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.tracy.mymall.common.dto.SkuEntityDto;
import com.tracy.mymall.common.dto.mq.QuickOrderDto;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.common.vo.MemberEntityVo;
import com.tracy.mymall.secondkill.dto.SeckillSessionsWithSkus;
import com.tracy.mymall.secondkill.dto.SeckillSkuRedisTo;
import com.tracy.mymall.secondkill.dto.SeckillSkuRelationEntity;
import com.tracy.mymall.secondkill.feign.MyMallProductFeignService;
import com.tracy.mymall.secondkill.feign.MymallCouponFeignService;
import com.tracy.mymall.secondkill.inteceptor.MyMallSecondkillInteceptor;
import com.tracy.mymall.secondkill.kafka.KafkaProducer;
import com.tracy.mymall.secondkill.service.SecondkillService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RSemaphore;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
@Slf4j
@Service
public class SecondkillServiceImpl implements SecondkillService {
    private static final String REDIS_SESSION_PRFIX = "SECKILL:SESSIONS: ";
    private static final String REDIS_SESSION_SKU_PRFIX = "SECKILL:SESSIONS_SKU";
    private static final String REDIS_SESSION_SKU_SEMPHOR_PRFIX = "SECKILL:SESSIONS_SKU_SEMPHOR:";
    private static final String REDIS_SESSION_LOCK = "SECKILL:LOCK";
    private static final String KAFKA_TOPIC_SECKILL = "SeckillTopic";
    @Autowired
    private KafkaProducer kafkaProducer;
    @Autowired
    private MymallCouponFeignService myMallCouponFeignService;

    @Autowired
    private MyMallProductFeignService myMallProductFeignService;

    @Autowired
    private Redisson redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 需要确保幂等性，确保分布式环境下数据只能被一个节点进行处理,保证数据不会重复
     */
    @Override
    public void uploadSeckillSkuLatest3Day() {
        RLock lock = redisson.getLock(REDIS_SESSION_LOCK);
        lock.lock();
        try {
            // 查询最近3天需要秒杀的场次和对应的商品
            R latest3DaysSession = myMallCouponFeignService.getLatest3DaysSession();
            if (latest3DaysSession.getCode() == 0) {
                List<SeckillSessionsWithSkus> sessionsWithSkuses = latest3DaysSession.get(new TypeReference<List<SeckillSessionsWithSkus>>() {});
                // 将秒杀商品信息缓存到redis中,需要注意不能重复保存
                // redis中的结构：使用队列list的方式存储秒杀场次
                // 使用hash的方式存储秒杀场次对应的秒杀商品

                saveSessionsInfo(sessionsWithSkuses);

                saveSessionAndSku(sessionsWithSkuses);

            }
        }finally {
            lock.unlock();
        }


    }

    /**
     * 从reeis中查出当前时间段的秒杀商品
     * @return
     */
    @Override
    public List<SeckillSkuRedisTo> getCurrentSeckillSkus() {
        List<SeckillSkuRedisTo> tos = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        // 获取key的前缀是
        Set<String> keys = redisTemplate.keys(REDIS_SESSION_PRFIX+"*");
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REDIS_SESSION_SKU_PRFIX);
        if (keys == null) {
            return tos;
        }
        keys.stream().forEach( key -> {
            String replace
                    = key.replace(REDIS_SESSION_PRFIX,"");
            String[] split = replace.split("_");
            Long start = Long.parseLong(split[0]);
            Long end = Long.parseLong(split[1]);
            if (currentTime >= start && currentTime < end) {
                List<String> range = redisTemplate.opsForList().range(key, 0, -1);
                //                    hashOps.multiGet(range) //获取多个数据

                for (String seckillProductId : range) {
                    String productValue = hashOps.get(seckillProductId);
                    SeckillSkuRedisTo to = JSON.parseObject(productValue, SeckillSkuRedisTo.class);
                    tos.add(to);
                }
            }

        });
        return tos;
    }

    /**
     * 根据skuId获取最近的秒杀信息，过期的消息不算
     * @param skuId
     * @return
     */
    @Override
    public SeckillSkuRedisTo getSkuSecondkillMsg(Long skuId) {
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(REDIS_SESSION_SKU_PRFIX);
        long currentTime = System.currentTimeMillis();

        // 获取所有的秒杀数据
        List<String> range = boundHashOps.values();

        if (range != null && range.size() > 0) {
            for (String seckillProduct : range) {
                SeckillSkuRedisTo to = JSON.parseObject(seckillProduct, SeckillSkuRedisTo.class);
                // 拿到当前商品最近的秒杀信息
                if (currentTime < to.getEndTime() && skuId.equals(to.getSkuId())) {
                    return to;

                }
            }
        }


        return null;
    }



    /**
     * redis中存储 starttime_endtime   {秒杀项的id(秒杀项包含一个商品)}
     * @param sessionsWithSkuses
     */
    private void saveSessionsInfo (List<SeckillSessionsWithSkus> sessionsWithSkuses) {
        sessionsWithSkuses.stream().forEach(session -> {
            long createTime = session.getStartTime().getTime();
            long endTime = session.getEndTime().getTime();
            String key = REDIS_SESSION_PRFIX + createTime + "_" + endTime;
            // redis中不存在key才写入
            if (!redisTemplate.hasKey(key)) {
                List<String> collect = session.getSkuRelationEntities().stream().map(item -> String.valueOf(item.getId())).collect(Collectors.toList());

                redisTemplate.opsForList().leftPushAll(key, collect);
            }

        });
    }

    /**
     * 在hash中存储每一个秒杀项的信息，一个秒杀项包含一个商品
     * @param sessionsWithSkuses
     */
    private void saveSessionAndSku(List<SeckillSessionsWithSkus> sessionsWithSkuses) {
        BoundHashOperations<String, Object, Object> boundHashOps = redisTemplate.boundHashOps(REDIS_SESSION_SKU_PRFIX);
        sessionsWithSkuses.stream().forEach(session -> {
            session.getSkuRelationEntities().stream().forEach( item -> {
                // redis中不出在才写入
                if (!boundHashOps.hasKey(item.getId().toString())) {
                    // 在hash中存放需要秒杀的每一个商品信息
                    // 秒杀的基本商品信息
                    SeckillSkuRedisTo seckillSkuRedisTo = new SeckillSkuRedisTo();

                    BeanUtils.copyProperties(item, seckillSkuRedisTo);
                    try {
                        // 远程查询秒杀的商品详细信息
                        R info = myMallProductFeignService.info(item.getSkuId());
                        Object skuInfo = info.get("skuInfo");
                        String string = JSON.toJSONString(skuInfo);
                        SkuEntityDto object = JSON.parseObject(string, new TypeReference<SkuEntityDto>(){});
                        seckillSkuRedisTo.setSkuInfoVo(object);
                    }catch(Exception e) {
                        log.error("", e);
                    }

                    //在商品信息里设置开始结束时间
                    seckillSkuRedisTo.setStartTime(session.getStartTime().getTime());
                    seckillSkuRedisTo.setEndTime(session.getEndTime().getTime());

                    // 设置随机码
                    String randomCode = UUID.randomUUID().toString().replace("-", "");
                    seckillSkuRedisTo.setRandomCode(randomCode);

                    // 分布式信号量
                    // 设置商品的信号量，用来限流，key是随机码，只有秒杀开始时候才暴露随机码，防止被恶意请求扣减

                    String semphorKey = REDIS_SESSION_SKU_SEMPHOR_PRFIX + randomCode;
                    //商品可以秒杀的数量作为信号量
                    redisson.getSemaphore(semphorKey).trySetPermits(item.getSeckillCount());

                    //  保存信息

                    boundHashOps.put(item.getId().toString(), JSON.toJSONString(seckillSkuRedisTo));
                }


            });

        });
    }

    /**
     * 秒杀服务，进行数据的校验，合法才允许秒杀，一件商品同一用户只允许秒杀一次
     * @param killId
     * @param key
     * @param num
     * @return
     */
    @Override
    public String seckill(Long killId, String key, Integer num) {
        MemberEntityVo memberEntityVo = MyMallSecondkillInteceptor.loginUser.get();
        BoundHashOperations<String, String, String> boundHashOps = redisTemplate.boundHashOps(REDIS_SESSION_SKU_PRFIX);

        // 验证killid是否存在
        String killProductMsg = boundHashOps.get(killId.toString());
        if (StringUtils.isEmpty(killProductMsg)) {
            return null;
        }

        SeckillSkuRedisTo skuRedisTo = JSON.parseObject(killProductMsg, SeckillSkuRedisTo.class);
        // 验证时间是否在秒杀范围之内
        long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis > skuRedisTo.getEndTime() || currentTimeMillis < skuRedisTo.getStartTime()) {
            return null;
        }
        // 验证key是否正确
        if (!key.equalsIgnoreCase(skuRedisTo.getRandomCode()) ) {
            return null;
        }
        // 验证商品数量是否超限制
        Integer seckillLimit = skuRedisTo.getSeckillLimit();
        if (num > seckillLimit) {
            return null;
        }
        // 3.验证这个人是否已经购买过了，没有参与过则写入用户参与秒杀信息
        String redisKey = memberEntityVo.getId() + "-" + killId;
        // 让数据自动过期
        long ttl = skuRedisTo.getEndTime() - skuRedisTo.getStartTime();
        Boolean aBoolean = redisTemplate.opsForValue().setIfAbsent(redisKey, num.toString(), ttl < 0 ? 0 : ttl, TimeUnit.MILLISECONDS);
        // aBoolean为false表示已经参与过当前商品的秒杀了
        if (!aBoolean) {
            return null;
        }


        // 开始秒杀
            // 获取信号量
        RSemaphore semaphore = redisson.getSemaphore(REDIS_SESSION_SKU_SEMPHOR_PRFIX+key);
        boolean tryAcquire = semaphore.tryAcquire();
        if (tryAcquire) {
            // 生成订单号,快速下单
            String orderSn = IdWorker.getTimeId();
            QuickOrderDto orderTo = new QuickOrderDto();
            orderTo.setOrderSn(orderSn);
            orderTo.setMemberId(memberEntityVo.getId());
            orderTo.setNum(num);
            orderTo.setSkuId(skuRedisTo.getSkuId());
            orderTo.setSeckillPrice(skuRedisTo.getSeckillPrice());
            orderTo.setPromotionSessionId(skuRedisTo.getPromotionSessionId());
            kafkaProducer.send(KAFKA_TOPIC_SECKILL, orderSn, orderTo);
            return orderSn;
            // 向消息队列发送生成订单成功
        }else {
            // 删除用户的秒杀信息
            redisTemplate.delete(redisKey);
            return null;
        }

    }

}
