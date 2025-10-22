package com.example.ishopping.config;

import com.example.ishopping.interceptor.UserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserInterceptor userInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**"); // 排除认证接口
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.1:5500", "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 处理静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations(
                        "classpath:/static/",
                        "classpath:/public/",
                        "file:C:/Users/19133/Downloads/Shopping/"
                );

        // 单独处理图片路径
        registry.addResourceHandler("/img/**")
                .addResourceLocations(
                        "classpath:/static/img/",
                        "classpath:/public/img/",
                        "file:C:/Users/19133/Downloads/Shopping/img/"
                );

        // 新增：添加上传文件的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}