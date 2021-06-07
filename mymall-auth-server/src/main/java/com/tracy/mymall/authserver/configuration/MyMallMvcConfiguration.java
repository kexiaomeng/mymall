package com.tracy.mymall.authserver.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MyMallMvcConfiguration implements WebMvcConfigurer {

    @Override
    public  void addViewControllers(ViewControllerRegistry registry) {
        // 第一个表示访问路径，第二个表示视图名
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");

    }
}
