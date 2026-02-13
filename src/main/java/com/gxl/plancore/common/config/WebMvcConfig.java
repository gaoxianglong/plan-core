package com.gxl.plancore.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.gxl.plancore.common.interceptor.AuthInterceptor;

/**
 * Web MVC 配置
 * 注册认证拦截器并配置白名单路径
 *
 * @author gxl
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebMvcConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/v1/user/login", "/api/v1/user/register", "/api/v1/user/forgot-password",
                        "/api/v1/user/refreshAccessToken", "/api/v1/user/session/check");
    }
}
