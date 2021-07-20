package com.tracy.mymall.order.configuration;

import com.tracy.mymall.order.inteceptor.OrderInteceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyMallWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private OrderInteceptor orderInteceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInteceptor).addPathPatterns("/**");
    }
}
