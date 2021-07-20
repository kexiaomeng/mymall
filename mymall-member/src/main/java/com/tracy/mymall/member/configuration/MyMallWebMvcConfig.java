package com.tracy.mymall.member.configuration;

import com.tracy.mymall.member.inteceptor.MemberInteceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyMallWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private MemberInteceptor orderInteceptor;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderInteceptor).addPathPatterns("/**");
    }
}
