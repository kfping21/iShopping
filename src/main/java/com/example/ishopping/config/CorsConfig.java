// config/CorsConfig.java
package com.example.ishopping.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://127.0.0.1:5500,http://localhost:5500}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers}")
    private String allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = getCorsConfiguration();

        // 对所有路径应用CORS配置
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    // 这个方法专门为 Spring Security 提供配置
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = getCorsConfiguration();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private CorsConfiguration getCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();

        // 设置允许的源
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);

        // 设置允许的HTTP方法
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        config.setAllowedMethods(methods);

        // 设置允许的请求头
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        config.setAllowedHeaders(headers);

        // 设置暴露的响应头（可选）
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition"));

        // 是否允许携带认证信息（cookies等）
        config.setAllowCredentials(allowCredentials);

        // 预检请求的缓存时间（秒）
        config.setMaxAge(maxAge);

        return config;
    }
}