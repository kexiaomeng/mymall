package com.tracy.mymall.order.configuration;

import org.springframework.context.annotation.Configuration;

/**
 * 如果需要定制kafka消费者的消费工厂和kafkaListenerContainerFactory，或者设置不自启动消费者、定制并发线程等可以在这个里面写
 */
@Configuration
public class KafkaConsumerConfig {
}
