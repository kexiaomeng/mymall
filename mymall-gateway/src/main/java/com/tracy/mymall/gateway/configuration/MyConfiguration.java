package com.tracy.mymall.gateway.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.Arrays;

@Configuration
public class MyConfiguration {
    /**
     * 网关层添加浏览器跨域请求过滤器，对所有的请求进行处理
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        CorsWebFilter webFilter = new CorsWebFilter(corsConfigurationSource);

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"));
        ((UrlBasedCorsConfigurationSource) corsConfigurationSource).registerCorsConfiguration("/**", corsConfiguration);

        return webFilter;
    }
}
