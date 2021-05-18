package com.tracy.mymall.product.configuration;

import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MyRedisConfiguration {

    @Bean
    public Redisson redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("localhost:6382").setDatabase(0);
        return (Redisson) Redisson.create(config);
    }
}
