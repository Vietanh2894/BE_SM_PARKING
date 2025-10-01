package com.example.stupidparking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Simple Configuration cho Face Recognition Service
 */
@Configuration
public class SimpleFaceRecognitionConfig {

    /**
     * RestTemplate bean for Face Recognition API calls
     */
    @Bean("faceRecognitionRestTemplate")
    public RestTemplate faceRecognitionRestTemplate() {
        return new RestTemplate();
    }

    /**
     * Face Recognition API base URL
     */
    @Bean("faceApiBaseUrl")
    public String faceApiBaseUrl() {
        return "http://localhost:8000";
    }
}