package com.tracy.mymall.order;

import com.alibaba.fastjson.JSON;
import com.tracy.mymall.order.entity.OrderEntity;
import com.tracy.mymall.order.kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.UUID;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.tracy.mymall.order.dao")
@EnableKafka
@EnableFeignClients
@EnableRedisHttpSession
@Slf4j
public class MymallOrderApplication {



    public static void main(String[] args) {
        SpringApplication.run(MymallOrderApplication.class, args);
    }


}
