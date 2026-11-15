package com.cardio.doctor.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:cdb216vl}")
    private String cloudName;

    @Value("${cloudinary.api-key:986679534219562}")
    private String apiKey;

    @Value("${cloudinary.api-secret:tr4Lq0XK1FpWmg7NaRaOOP1iwpI}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true");

        return new Cloudinary(config);
    }
}
