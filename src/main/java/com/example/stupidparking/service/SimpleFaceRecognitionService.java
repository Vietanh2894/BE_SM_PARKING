package com.example.stupidparking.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.HashMap;

/**
 * Face Recognition Service using RestTemplate (alternative to WebClient)
 */
@Service
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SimpleFaceRecognitionService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public SimpleFaceRecognitionService(
            @Qualifier("faceRecognitionRestTemplate") RestTemplate restTemplate,
            @Qualifier("faceApiBaseUrl") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Health check
     */
    public Map<String, Object> checkHealth() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl + "/api/v1/simple-face/health", Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Register face
     */
    public Map<String, Object> registerFace(String name, String base64Image, String description) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("name", name);
            requestBody.put("image", base64Image);
            requestBody.put("description", description);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/simple-face/register", request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Register face from file upload (multipart/form-data)
     */
    public Map<String, Object> registerFaceFromFile(String name, byte[] imageBytes, String description) {
        try {
            // Create multipart form data
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("name", name);
            if (description != null) {
                body.add("description", description);
            }

            // Add file as ByteArrayResource
            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "face.jpg"; // Provide a filename
                }
            };
            body.add("file", imageResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/simple-face/register-file", request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Recognize face
     */
    public Map<String, Object> recognizeFace(String base64Image, Double threshold) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image", base64Image);
            requestBody.put("threshold", threshold != null ? threshold : 0.6);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/simple-face/recognize", request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Compare faces
     */
    public Map<String, Object> compareFaces(String base64Image1, String base64Image2, Double threshold) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("image1", base64Image1);
            requestBody.put("image2", base64Image2);
            requestBody.put("threshold", threshold != null ? threshold : 0.6);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    baseUrl + "/api/v1/simple-face/compare", request, Map.class);

            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * List registered faces
     */
    public Map<String, Object> listRegisteredFaces() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    baseUrl + "/api/v1/simple-face/list", Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Delete face
     */
    public Map<String, Object> deleteFace(Long faceId) {
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/api/v1/simple-face/delete/" + faceId,
                    HttpMethod.DELETE,
                    null,
                    Map.class);
            return response.getBody();
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return errorResponse;
        }
    }
}