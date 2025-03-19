package com.bnm.recouvrement.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir les fichiers de chèque depuis le système de fichiers
        registry.addResourceHandler("/cheques/**")
                .addResourceLocations("file:./" + uploadDir + "/cheques/");

        // Servir les fichiers de garantie
        registry.addResourceHandler("/garanties/**")
                .addResourceLocations("file:./" + uploadDir + "/garanties/");

  registry.addResourceHandler("/credits/**")
                .addResourceLocations("file:./" + uploadDir + "/credits/");
        // Servir d'autres fichiers
        registry.addResourceHandler("/cautions/**")
        .addResourceLocations("file:./" + uploadDir + "/cautions/");
    }
}