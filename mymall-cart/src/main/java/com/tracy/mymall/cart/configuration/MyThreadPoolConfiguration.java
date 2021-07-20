package com.tracy.mymall.cart.configuration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class MyThreadPoolConfiguration {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor(ThreadPoolProperties poolProperties) {
        return new ThreadPoolExecutor(poolProperties.getCoreSize(), poolProperties.getMaxSize(),
                poolProperties.getKeepAliveTime(), TimeUnit.SECONDS, new ArrayBlockingQueue<>(100), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }
}
