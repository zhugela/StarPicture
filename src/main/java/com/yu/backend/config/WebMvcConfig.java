package com.yu.backend.config;

import com.yu.backend.interceptor.SpaceUserAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private SpaceUserAuthInterceptor spaceUserAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(spaceUserAuthInterceptor)
                .addPathPatterns("/**");
    }
}
