package com.tracy.mymall.thirdpart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class MymallThirdpartApplication {

    public static void main(String[] args) {
        SpringApplication.run(MymallThirdpartApplication.class, args);
    }

}
