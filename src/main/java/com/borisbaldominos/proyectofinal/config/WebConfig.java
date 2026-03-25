package com.borisbaldominos.proyectofinal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Esto le dice a Spring: "Cuando alguien pida /uploads/algo, busca en la carpeta física uploads del proyecto"
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}