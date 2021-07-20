package com.tracy.mymall.ware.kafka;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.tracy.mymall.common.dto.mq.WareLockedDto;
import com.tracy.mymall.common.enums.OrderStatusEnum;
import com.tracy.mymall.common.utils.R;
import com.tracy.mymall.ware.entity.WareOrderTaskDetailEntity;
import com.tracy.mymall.ware.entity.WareOrderTaskEntity;
import com.tracy.mymall.ware.feign.MyMallOrderFeignService;
import com.tracy.mymall.ware.service.WareOrderTaskDetailService;
import com.tracy.mymall.ware.service.WareOrderTaskService;
import com.tracy.mymall.ware.service.WareSkuService;
import com.tracy.mymall.ware.vo.OrderVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * 构建延时队列，确保延迟队列只有一个分区，发送到延时队列的消息是按照时间戳顺序存储的，如果延时队列中的消息没有过期，则取消消费，
 * 设置一个定时任务，定时开启消费判断，如果延时队列中的消息过期了，则将消息发送到死信队列，
 * 如果没有过期，则取消消费，并将offset设置到没有过期的消息的offset
 */

/**
 * 注释掉的几个@KafkaListener都是可以使用的
 */
@Component
@Slf4j
public class KafkaConsumer {

    public final static String lockedTopic = "StockLockedTopic";
    public final static String releaseTopic = "StockedReleaseTopic";
    private final static int DELAY_TIME = 120;
    private ScheduledExecutorService service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private WareOrderTaskService taskService;

    @Autowired
    WareSkuService wareSkuService;

    @Autowired
    private WareOrderTaskDetailService detailService;


    @Autowired
    private MyMallOrderFeignService orderFeignService;
    /**
     * 构建延时队列,方法需要和定时唤醒的方法进行同步
     * @param record
     * @param acknowledgment
     * @param consumer
     */
    @KafkaListener(id = lockedTopic, topics = {lockedTopic})
    public void consumerStockLockedTopic(ConsumerRecord<String, String> record, Acknowledgment acknowledgment ,Consumer<String, String> consumer) {
        if (registry.getListenerContainer(lockedTopic).isContainerPaused()) {
            // 如果有消息已经暂停了消费，则不处理当前消息
            return;
        }
        String topic = record.topic();
        int partition = record.partition();
        TopicPartition topicPartition = new TopicPartition(topic, partition);
        long offset = record.offset();
        long timestamp = record.timestamp();
        long duration = Math.abs(Duration.between(Instant.ofEpochMilli(timestamp), Instant.ofEpochMilli(System.currentTimeMillis())).getSeconds());
        if (duration > DELAY_TIME) {
            // 消息过期了,则将消息发送到死信队列
            log.info("【库存】库存锁定延时消息到期，开始处理： [{}]", record.value());
            try {
                WareLockedDto wareLockedDto = JSONObject.parseObject(record.value(), WareLockedDto.class);

                kafkaProducer.send(releaseTopic, wareLockedDto.getTaskId().toString(),  wareLockedDto);
            }catch(Exception e) {
                log.error("", e);
            }

            // 手动提交当前partition的offset
            // success. commit message
            OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset + 1);
            HashMap<TopicPartition, OffsetAndMetadata> metadataHashMap = new HashMap<>();
            metadataHashMap.put(topicPartition, offsetAndMetadata);
            consumer.commitSync(metadataHashMap);

        } else {
            log.debug("【库存】[{}]暂停消费", record.topic());

            // 消息没有过期，需要暂停消费,将offset重新定位到当前
            registry.getListenerContainer(lockedTopic).pause();
            OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset);
            consumer.seek(topicPartition, offsetAndMetadata);

            // 启动异步的定时任务，用来激活消费者// 此处由于kafka的多并发限制，如果用不同的线程直接操作consumer暂停和启动分区的消费，会失败，不可以在不同线程异步任务中使用consumer.resume
            service.schedule(() -> {
                log.debug("【库存】[{}],重新启动消费", record.topic());
                registry.getListenerContainer(lockedTopic).resume();
            }, (DELAY_TIME - duration) + 1 , TimeUnit.SECONDS);

        }
    }

    /**
     * 为了确保订单和库存得一致性，需要两种解锁库存得方法配合实现库存得解锁：1.订单关闭后主动通知，2. 库存服务自动解锁，第二种是为了防止订单没有生成但是库存减了得情况，第一种是为了防止订单服务阻塞了，造成库存得自动解锁服务先执行，发现订单时new状态造成没有解锁得问题。
     * 根据一系列条件判断是否需要解锁库存
     * 1. 下订单成功，但是用户超时未支付，或者用户取消订单
     * 2. 下订单扣减库存成功，但是订单服务由于异常原因回退，实际没有生成订单，但是扣了库存
     * @param record
     * @param ack
     */
    @KafkaListener(id = releaseTopic, topics = {releaseTopic})
    public void consumerReleaseTopic(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("【库存】解锁服务收到消息[{}],开始处理", record.value());
        String value = record.value();
        try {
            if (value.contains("stockDetailTo")) {
                this.unlockStockByWareSelf(value, ack);
            }else {
                this.unlockStockByOrderNotify(value, ack);
            }
        }catch(Exception e) {
            // 远程查询失败后需要重新消费当前数据
            ack.nack(1000L);
            log.error("", e);
        }
    }

    /**
     * 订单关闭后主动通知解锁库存
     */
    @Transactional
    public void unlockStockByOrderNotify(String value, Acknowledgment ack) {
        log.info("收到订单通知关闭消息 [{}]", value);
        OrderVo orderVo = JSONObject.parseObject(value, OrderVo.class);
        String orderSn = orderVo.getOrderSn();
        // 根据订单编号查询出订单对应得库存任务单
        try {
            WareOrderTaskEntity task = taskService.getTaskByOrderSn(orderSn);

            List<WareOrderTaskDetailEntity> detailEntities =  detailService.queryDetailListByTaskId(task.getId());
            for (WareOrderTaskDetailEntity detailEntity : detailEntities) {
                // 不是解锁状态才进行解锁
                if (detailEntity.getLockStatus() == 1) {
                    unlockStock(detailEntity.getSkuId(), detailEntity.getSkuNum() , detailEntity.getWareId(), detailEntity.getId());
                }
            }
        }catch(Exception e) {
            log.error("", e);
        }

        ack.acknowledge();

    }

    /**
     * 库存服务主动解锁库存，防止订单未创建成功，但是减了库存得情况
     */
    private void unlockStockByWareSelf(String value, Acknowledgment ack) {
        WareLockedDto wareLockedDto = JSONObject.parseObject(value, WareLockedDto.class);

        Long detailId = wareLockedDto.getStockDetailTo().getId();

        WareOrderTaskDetailEntity detailEntity = detailService.getById(detailId);

        if (detailEntity != null) {
            // 根据相关条件去解锁库存
            // taskEntity不可能为空
            WareOrderTaskEntity taskEntity = taskService.getById(wareLockedDto.getTaskId());
            // 根据orderSn远程查询订单状态
            String orderSn = taskEntity.getOrderSn();

            R result = orderFeignService.queryOrderByOrderSn(orderSn);
            if (result.getCode() == 0) {
                String data = (String) result.get("data");
                OrderVo orderVo = JSON.parseObject(data, OrderVo.class);
                // 检查状态，如果是已取消，并且状态不是已解锁，就就行库存的回退
                if (orderVo.getStatus().equals(OrderStatusEnum.CANCLED.getCode()) && detailEntity.getLockStatus() == 1 ) {
                    unlockStock( detailEntity.getSkuId(), detailEntity.getSkuNum(), detailEntity.getWareId(), detailId);
                }

            }else if (result.getCode() == 600){
                // 说明没有订单，直接解锁库存
                if ( detailEntity.getLockStatus() == 1) {
                    unlockStock( detailEntity.getSkuId(), detailEntity.getSkuNum(), detailEntity.getWareId(), detailId);

                }

            }else {
                // 远程查询失败后需要重新消费当前数据
                ack.nack(1000L);
            }
        }

    }
    /**
     * 解锁库存
     * @param skuId
     * @param skuNum
     * @param wareId
     */
    private void unlockStock(Long skuId, Integer skuNum, Long wareId , Long detailId) {

        wareSkuService.unlockStock(skuId, skuNum, wareId, detailId);
    }


}
