package com.tracy.mymall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.tracy.mymall.order.dao")
public class MymallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymallOrderApplication.class, args);
    }

}
