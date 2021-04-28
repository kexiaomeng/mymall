package com.tracy.mymall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MymallGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymallGatewayApplication.class, args);
    }

}
