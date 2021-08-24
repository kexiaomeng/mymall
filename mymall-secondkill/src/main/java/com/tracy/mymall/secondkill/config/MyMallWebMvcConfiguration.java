package com.tracy.mymall.secondkill.config;

import com.tracy.mymall.secondkill.inteceptor.MyMallSecondkillInteceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class MyMallWebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private MyMallSecondkillInteceptor myMallSecondkillInteceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(myMallSecondkillInteceptor).addPathPatterns("/**");

    }
}
