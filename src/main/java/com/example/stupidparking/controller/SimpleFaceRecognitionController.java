package com.example.stupidparking.controller;

import com.example.stupidparking.service.SimpleFaceRecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;

/**
 * Simple Face Recognition Controller using RestTemplate
 */
@RestController
@RequestMapping("/api/v1/simple-face")
@CrossOrigin(origins = "*")
public class SimpleFaceRecognitionController {

    @Autowired
    private SimpleFaceRecognitionService faceService;

    /**
     * Test endpoint
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Simple Face Recognition Controller is working!");
        return ResponseEntity.ok(response);
    }

    /**
     * Test register-file endpoint integration
     */
    @GetMapping("/test-register-file")
    public ResponseEntity<Map<String, Object>> testRegisterFile() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Register-file endpoint is ready!");
        response.put("info", "Use POST /api/v1/simple-face/register-file with multipart form data");
        response.put("fix_applied", "Now calls Python /register-file endpoint instead of /register");
        return ResponseEntity.ok(response);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = faceService.checkHealth();
        return ResponseEntity.ok(response);
    }

    /**
     * Register face with base64
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerFace(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        String image = (String) request.get("image");
        String description = (String) request.get("description");

        Map<String, Object> response = faceService.registerFace(name, image, description);

        Boolean success = (Boolean) response.get("success");
        if (success != null && success) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Register face with file upload
     */
    @PostMapping("/register-file")
    public ResponseEntity<Map<String, Object>> registerFaceFromFile(
            @RequestParam("name") String name,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "description", required = false) String description) {

        try {
            // Use new method for file upload that calls Python /register-file endpoint
            Map<String, Object> response = faceService.registerFaceFromFile(name, imageFile.getBytes(), description);

            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process image: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Recognize face with base64
     */
    @PostMapping("/recognize")
    public ResponseEntity<Map<String, Object>> recognizeFace(@RequestBody Map<String, Object> request) {
        String image = (String) request.get("image");
        Double threshold = request.get("threshold") != null ? Double.valueOf(request.get("threshold").toString()) : 0.6;

        Map<String, Object> response = faceService.recognizeFace(image, threshold);

        Boolean success = (Boolean) response.get("success");
        if (success != null && success) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Recognize face with file upload
     */
    @PostMapping("/recognize-file")
    public ResponseEntity<Map<String, Object>> recognizeFaceFromFile(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "threshold", required = false, defaultValue = "0.6") Double threshold) {

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            Map<String, Object> response = faceService.recognizeFace(base64Image, threshold);

            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Failed to process image: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Compare faces with base64
     */
    @PostMapping("/compare")
    public ResponseEntity<Map<String, Object>> compareFaces(@RequestBody Map<String, Object> request) {
        String image1 = (String) request.get("image1");
        String image2 = (String) request.get("image2");
        Double threshold = request.get("threshold") != null ? Double.valueOf(request.get("threshold").toString()) : 0.6;

        Map<String, Object> response = faceService.compareFaces(image1, image2, threshold);

        Boolean success = (Boolean) response.get("success");
        if (success != null && success) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * List registered faces
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listRegisteredFaces() {
        Map<String, Object> response = faceService.listRegisteredFaces();
        return ResponseEntity.ok(response);
    }

    /**
     * Delete face
     */
    @DeleteMapping("/delete/{faceId}")
    public ResponseEntity<Map<String, Object>> deleteFace(@PathVariable Long faceId) {
        Map<String, Object> response = faceService.deleteFace(faceId);

        Boolean success = (Boolean) response.get("success");
        if (success != null && success) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}