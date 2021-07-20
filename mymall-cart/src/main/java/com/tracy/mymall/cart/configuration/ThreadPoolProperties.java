package com.tracy.mymall.cart.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 自定义配置信息，从配置文件读取,添加maven依赖>spring-boot-configuration-processor与application配置文件绑定
 */
@Data
@ConfigurationProperties(prefix = "mymall.threadpool")
public class ThreadPoolProperties {

    private int coreSize;
    private int maxSize;
    private int keepAliveTime;
}
