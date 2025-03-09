package com.bnm.recouvrement.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les fichiers de chèque
        registry.addResourceHandler("/cheques/**")
        .addResourceLocations("file:C:/Users/DELL/Desktop/bnm/dossiers-recouvrement-backend/uploads/cheques/");

        // Servir les fichiers de garantie
        registry.addResourceHandler("/garanties/**")
                .addResourceLocations("file:uploads/garanties/");

        // Servir d'autres fichiers
        registry.addResourceHandler("/autres/**")
                .addResourceLocations("file:uploads/autres/");
    }
}