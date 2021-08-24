package com.tracy.mymall.order.kafka;

import com.alibaba.fastjson.JSON;
import com.tracy.mymall.common.dto.mq.QuickOrderDto;
import com.tracy.mymall.common.enums.OrderStatusEnum;
import com.tracy.mymall.order.entity.OrderEntity;
import com.tracy.mymall.order.entity.OrderItemEntity;
import com.tracy.mymall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
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
//@KafkaListener(topics = {"topic1"}) // 注解在类上配合kafkahandler使用
public class KafkaConsumer {
    public final static String lockedTopic = "OrderCreateTopic";
    public final static String releaseTopic = "OrderReleaseTopic";
    public final static String stockReleaseTopic = "StockedReleaseTopic";
    private static final String KAFKA_TOPIC_SECKILL = "SeckillTopic";

    private final static int DELAY_TIME = 60;

    private ScheduledExecutorService service = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Autowired
    private OrderService orderService;



    /**
     * 构建延时队列,方法需要和定时唤醒的方法进行同步
     * @param record
     * @param acknowledgment
     * @param consumer
     */
    @KafkaListener(id = lockedTopic, topics = {lockedTopic})
    public void consumerCreateTopic(ConsumerRecord<String, String> record, Acknowledgment acknowledgment ,Consumer<String, String> consumer) {
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
            OrderEntity orderEntity = JSON.parseObject(record.value(), OrderEntity.class);
            String value = record.value();
            kafkaProducer.send(releaseTopic, orderEntity.getOrderSn(),  orderEntity);
            log.info("延时消息到期，开始处理:[{}]", value);

            // 手动提交当前partition的offset
            // success. commit message
            OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset + 1);
            HashMap<TopicPartition, OffsetAndMetadata> metadataHashMap = new HashMap<>();
            metadataHashMap.put(topicPartition, offsetAndMetadata);
            consumer.commitSync(metadataHashMap);
//            或者
//            ack.acknowledge();

        } else {
            log.info("[{}]暂停消费", record.topic());

            // 消息没有过期，需要暂停消费,将offset重新定位到当前
            registry.getListenerContainer(lockedTopic).pause();
            OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(offset);
            consumer.seek(topicPartition, offsetAndMetadata);

            // 启动异步的定时任务，用来激活消费者// 此处由于kafka的多并发限制，如果用不同的线程直接操作consumer暂停和启动分区的消费，会失败，不可以在不同线程异步任务中使用consumer.resume
            service.schedule(() -> {
                log.debug("【库存】[{}],重新启动消费", record.topic());
                registry.getListenerContainer(lockedTopic).resume();
            }, DELAY_TIME - duration + 1 , TimeUnit.SECONDS);

        }
    }

    @KafkaListener(id = releaseTopic, topics = {releaseTopic})
    public void consumerReleaseTopic(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("【订单】解锁服务收到消息[{}],开始处理", record.value());
        try {
            OrderEntity orderEntity = JSON.parseObject(record.value(), OrderEntity.class);
            OrderEntity realOrder = orderService.getById(orderEntity.getId());
            if (realOrder.getStatus().equals(OrderStatusEnum.CREATE_NEW.getCode()) ) {
                realOrder.setStatus(OrderStatusEnum.CANCLED.getCode());
                orderService.updateById(realOrder);
                // 关闭订单后向库存解锁对应得topic发送消息，进行库存的解锁
                kafkaProducer.send(stockReleaseTopic, realOrder.getOrderSn(), realOrder);
            }
            ack.acknowledge();
        }catch(Exception e) {
            // 失败后需要重新消费当前数据
            ack.nack(1000L);
            log.error("", e);
        }


    }

    @KafkaListener(id = KAFKA_TOPIC_SECKILL, topics = {KAFKA_TOPIC_SECKILL})
    public void consumerSeckillTopic(ConsumerRecord<String, String> record, Acknowledgment ack) {
        log.info("【订单】收到秒杀订单消息[{}],开始处理", record.value());
        if (!record.value().contains("seckillPrice")) {
            ack.acknowledge();
            return;
        }
        try {
            QuickOrderDto quickOrderDto = JSON.parseObject(record.value(), QuickOrderDto.class);
            orderService.createSeckillOrder(quickOrderDto);

            ack.acknowledge();
        }catch(Exception e) {
            // 失败后需要重新消费当前数据
            ack.nack(1000L);
            log.error("", e);
        }


    }


//    @KafkaHandler
//    public void consumerMsg3(String value) {
//        System.out.println("value"+" "+value);
//    }

//    @KafkaHandler(isDefault = true)
//    public void consumerMsg2(ConsumerRecord<String, String> record) {
//        System.out.println("default"+" "+record.topic()+" "+record.partition()+" "+record.key()+" "+record.offset()+" "+record.value());
//    }

//    @KafkaListener(topics = {"topic1"})
//    public void consumerMsg2(String value) {
//        System.out.println("value"+" "+value);
//    }

//    @KafkaListener(topics = {"topic1"})
//    // 开启批量监听时才可以使用形参List<ConsumerRecord<String, String>> records
//    public void consumerMsg3(List<ConsumerRecord<String, String>> records) {
//        System.out.println(records.toString());
//        records.forEach(record -> {
//            System.out.println("records"+" "+record.topic()+" "+record.partition()+" "+record.key()+" "+record.offset()+" "+record.value());
//
//        });
//    }
}
