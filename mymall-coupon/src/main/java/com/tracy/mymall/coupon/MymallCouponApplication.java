package com.tracy.mymall.coupon;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.tracy.mymall.coupon.dao")
public class MymallCouponApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymallCouponApplication.class, args);
    }

}
