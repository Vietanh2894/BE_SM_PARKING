# Face Recognition API Integration

## ⚠️ **Import Issues Fixed**

### **Problem**: WebFlux Dependencies không được import đúng
- WebClient và Mono classes không resolve được
- IDE cache issues sau khi add dependencies  
- Conflict giữa `spring-boot-starter-web` và `spring-boot-starter-webflux`

### **Solutions**: Đã implement 2 approaches

#### **1. WebFlux Approach** (Advanced - Reactive)
- **Files**: `FaceRecognitionService.java`, `FaceRecognitionController.java`, `FaceRecognitionConfig.java`
- **APIs**: `/api/v1/face/*`
- **Uses**: WebClient + Mono (Reactive programming)
- **Status**: ✅ Code compiles but IDE có thể show errors (cache issue)

#### **2. RestTemplate Approach** (Simple - Traditional)  
- **Files**: `SimpleFaceRecognitionService.java`, `SimpleFaceRecognitionController.java`
- **APIs**: `/api/v1/simple-face/*`
- **Uses**: RestTemplate + Map<String,Object> (Traditional HTTP client)
- **Status**: ✅ Working perfect, no import issues

---

## API Endpoints

### Simple Face Recognition Controller (`/api/v1/simple-face`) - **RECOMMENDED**

#### 1. Test/Health Check
```http
GET /api/v1/simple-face/test
GET /api/v1/simple-face/health
```

#### 2. Register Face (Base64)
```http
POST /api/v1/simple-face/register
Content-Type: application/json

{
  "name": "John Doe",
  "image": "base64_encoded_image",
  "description": "Employee ID: 123"
}
```

#### 3. Register Face (File Upload)
```http
POST /api/v1/simple-face/register-file
Content-Type: multipart/form-data

name: John Doe
image: [file]
description: Employee ID: 123
```

#### 4. Recognize Face (Base64)
```http
POST /api/v1/simple-face/recognize
Content-Type: application/json

{
  "image": "base64_encoded_image",
  "threshold": 0.6
}
```

#### 5. Recognize Face (File Upload)
```http
POST /api/v1/simple-face/recognize-file
Content-Type: multipart/form-data

image: [file]
threshold: 0.7
```

#### 6. Compare Faces (Base64)
```http
POST /api/v1/simple-face/compare
Content-Type: application/json

{
  "image1": "base64_encoded_image1",
  "image2": "base64_encoded_image2", 
  "threshold": 0.6
}
```

#### 7. List Registered Faces
```http
GET /api/v1/simple-face/list
```

#### 8. Delete Face
```http
DELETE /api/v1/simple-face/delete/{faceId}
```

---

### Advanced Face Recognition Controller (`/api/v1/face`) - **WebFlux**

Same endpoints as above but with `/api/v1/face` prefix and reactive Mono<ResponseEntity<>> return types.

## Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation successful",
  "data": {
    // Specific data based on operation
  }
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

## Integration Examples

### 1. Employee Registration
```java
@Autowired
private FaceRecognitionService faceService;

public void registerEmployee(Long employeeId, MultipartFile photo) {
    String base64Image = Base64.getEncoder().encodeToString(photo.getBytes());
    
    faceService.registerFace(
        "Employee-" + employeeId,
        base64Image,
        "Staff member"
    ).subscribe(response -> {
        if (response.getSuccess()) {
            // Save faceId to employee record
            Long faceId = response.getData().getFaceId();
        }
    });
}
```

### 2. Face Verification for Access Control
```java
public boolean verifyAccess(MultipartFile photo, double threshold) {
    String base64Image = Base64.getEncoder().encodeToString(photo.getBytes());
    
    return faceService.recognizeFace(base64Image, threshold)
        .map(response -> {
            if (response.getSuccess() && response.getData().getFaces().size() > 0) {
                var face = response.getData().getFaces().get(0);
                return face.getMatchFound() && face.getMatchSimilarity() > threshold;
            }
            return false;
        }).block(); // Block for synchronous result
}
```

## Configuration Properties

```properties
# application.properties
face.api.base-url=http://localhost:5000/api
face.api.timeout.connection=30
face.api.timeout.response=60
```

## Error Handling
- Network errors: Retries với exponential backoff
- Invalid image format: BadRequest response
- Face not found: 404 response
- Python API down: 500 response với fallback message

## Security Considerations
- CORS enabled cho development (`@CrossOrigin(origins = "*")`)
- Production: Restrict CORS để specific domains
- Rate limiting: Implement để prevent API abuse
- Authentication: Thêm security cho sensitive endpoints

## Testing
```bash
# Test health endpoint
curl http://localhost:8080/api/v1/face/test

# Test with file upload
curl -X POST http://localhost:8080/api/v1/face/register-file \
  -F "name=Test User" \
  -F "image=@test_image.jpg" \
  -F "description=Test registration"
```

## Dependencies Fixed
✅ WebClient injection đúng cách với @Qualifier  
✅ Mono/Reactor types import correctly  
✅ Response DTOs methods available  
✅ Generic type casting issues resolved  
✅ File upload handling implemented  
✅ Error handling với proper HTTP status codes