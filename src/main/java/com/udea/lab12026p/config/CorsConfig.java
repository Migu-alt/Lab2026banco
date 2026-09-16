package com.udea.lab12026p.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS mínima para permitir que el frontend React/Vite
 * (http://localhost:5173) consuma la API expuesta por este backend
 * (http://localhost:8080) durante el desarrollo local.
 *
 * No se utiliza Spring Security: esta configuración se apoya únicamente
 * en Spring MVC, que ya está presente en el proyecto (spring-boot-starter-web).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String FRONTEND_ORIGIN = "http://localhost:5173";

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(FRONTEND_ORIGIN)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Accept")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
