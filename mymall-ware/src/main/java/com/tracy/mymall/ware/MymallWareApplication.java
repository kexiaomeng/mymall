package com.tracy.mymall.ware;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication

@EnableDiscoveryClient
@EnableTransactionManagement
@EnableFeignClients
@MapperScan("com.tracy.mymall.ware.dao")
@EnableKafka
public class MymallWareApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymallWareApplication.class, args);
    }

}
