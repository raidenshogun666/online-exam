package com.example.online_exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许Vite前端域名（本地开发默认5173端口）
        config.addAllowedOriginPattern("*");
        // 允许携带Cookie（核心：Session需要JSESSIONID）
        config.setAllowCredentials(true);
        // 允许所有请求方法（GET/POST等）
        config.addAllowedMethod("*");
        // 允许所有请求头
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // 所有接口都允许跨域
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}