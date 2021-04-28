package com.tracy.mymall.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.tracy.mymall.member.dao")
@EnableFeignClients
@EnableHystrix
public class MymallMemberApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymallMemberApplication.class, args);
    }

}
