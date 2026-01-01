package com.xiaowang.xwpicturebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许跨域请求携带cookie
                .allowCredentials(true)
                // 允许跨域请求的域名
                .allowedOriginPatterns("*")
                // 允许跨域请求的方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 允许跨域请求的头信息
                .allowedHeaders("*")
                .exposedHeaders("*");
    }
}
