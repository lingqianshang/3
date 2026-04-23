package com.example.demo.config;

import com.example.demo.interceptor.AuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/api/users/**")
                .excludePathPatterns(
                        "/api/users",               // 注册 POST
                        "/api/users/login",         // 登录 POST
                        "/api/users/page",          // 分页 GET
                        "/api/users/*/detail",      // 用户详情 GET (多表联查+缓存)
                        "/api/users/*/info",        // 更新扩展信息 PUT
                        "/api/users/*"              // 删除用户 DELETE (注意顺序, 放在最后)
                );
    }
}