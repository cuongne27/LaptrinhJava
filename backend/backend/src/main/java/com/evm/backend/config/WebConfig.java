// src/main/java/com/evm/backend/config/WebConfig.java
package com.evm.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ✅ Lấy absolute path của thư mục uploads
        String uploadPath = Paths.get("uploads/images/").toAbsolutePath().toString();

        System.out.println("📁 Serving static files from: " + uploadPath);

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + uploadPath + "/")
                .setCachePeriod(3600);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // ✅ Cho phép CORS cho images
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}