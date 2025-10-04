package nckh.felix.StupidParking.service;

import com.example.stupidparking.service.SimpleFaceRecognitionService;
import nckh.felix.StupidParking.domain.DangKyThang;
import nckh.felix.StupidParking.domain.ParkingTransaction;
import nckh.felix.StupidParking.util.error.IdInvalidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Service tích hợp Face Recognition vào business logic của hệ thống
 */
@Service
public class FaceRecognitionIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(FaceRecognitionIntegrationService.class);

    @Autowired
    private SimpleFaceRecognitionService faceRecognitionService;

    /**
     * Đăng ký khuôn mặt cho đăng ký tháng mới
     * 
     * @param dangKyThang     Đăng ký tháng
     * @param faceImageBase64 Ảnh khuôn mặt dạng base64
     * @return Kết quả đăng ký (success/failure)
     */
    public FaceRegistrationResult registerFaceForMonthlyRegistration(DangKyThang dangKyThang, String faceImageBase64) {
        try {
            System.out.println(" [FaceService] Bắt đầu đăng ký face cho CCCD: " + dangKyThang.getCccd());

            if (faceImageBase64 == null || faceImageBase64.trim().isEmpty()) {
                System.out.println(" [FaceService] Ảnh khuôn mặt rỗng");
                return new FaceRegistrationResult(false, "Ảnh khuôn mặt không được để trống", null, null);
            }

            System.out.println(" [FaceService] Face image length: " + faceImageBase64.length());
            System.out.println(" [FaceService] Face image prefix: "
                    + faceImageBase64.substring(0, Math.min(30, faceImageBase64.length())));

            // Tạo tên unique cho face registration
            String faceName = "MonthlyReg_" + dangKyThang.getBienSoXe() + "_" + dangKyThang.getCccd();
            String description = "Đăng ký tháng - CCCD: " + dangKyThang.getCccd() +
                    ", Biển số: " + dangKyThang.getBienSoXe() +
                    ", Ngày đăng ký: " + LocalDateTime.now();

            System.out.println(" [FaceService] Face name: " + faceName);
            System.out.println(" [FaceService] Description: " + description);

            // Gọi FastAPI để đăng ký khuôn mặt
            System.out.println(" [FaceService] Gọi FastAPI registerFace...");
            Map<String, Object> response = faceRecognitionService.registerFace(faceName, faceImageBase64, description);

            System.out.println(" [FaceService] FastAPI Response: " + response);

            Boolean success = (Boolean) response.get("success");
            System.out.println(" [FaceService] Success status: " + success);

            if (success != null && success) {
                // Lấy face_id từ response
                String faceId = extractFaceIdFromResponse(response);
                BigDecimal similarity = extractSimilarityFromResponse(response);

                System.out.println(" [FaceService] Extracted Face ID: " + faceId);
                System.out.println(" [FaceService] Extracted Similarity: " + similarity);

                if (faceId != null) {
                    // Cập nhật DangKyThang với thông tin face
                    System.out.println(" [FaceService] Cập nhật DangKyThang object...");
                    dangKyThang.setFaceId(faceId);
                    dangKyThang.setFaceSimilarity(similarity);
                    dangKyThang.setFaceRegisteredDate(LocalDateTime.now());

                    System.out.println(" [FaceService] Đã cập nhật object - Face ID: " + dangKyThang.getFaceId());
                    System.out.println(
                            " [FaceService] Đã cập nhật object - Similarity: " + dangKyThang.getFaceSimilarity());

                    logger.info("Đăng ký khuôn mặt thành công cho CCCD: {}, Face ID: {}",
                            dangKyThang.getCccd(), faceId);

                    return new FaceRegistrationResult(true, "Đăng ký khuôn mặt thành công", faceId, similarity);
                } else {
                    System.out.println(" [FaceService] Không nhận được Face ID từ FastAPI");
                    return new FaceRegistrationResult(false, "Không nhận được Face ID từ FastAPI", null, null);
                }
            } else {
                String errorMessage = (String) response.get("message");
                System.out.println(" [FaceService] FastAPI trả về thất bại: " + errorMessage);
                return new FaceRegistrationResult(false, "Đăng ký khuôn mặt thất bại: " + errorMessage, null, null);
            }

        } catch (Exception e) {
            System.out.println(" [FaceService] Exception: " + e.getMessage());
            e.printStackTrace();
            logger.error("Lỗi khi đăng ký khuôn mặt cho CCCD: {}", dangKyThang.getCccd(), e);
            return new FaceRegistrationResult(false, "Lỗi hệ thống: " + e.getMessage(), null, null);
        }
    }

    /**
     * Đăng ký khuôn mặt tạm thời cho xe vãng lai
     * 
     * @param faceImageBase64 Ảnh khuôn mặt dạng base64
     * @param visitorId       ID tạm thời cho xe vãng lai (có thể dùng biển số xe)
     * @return Kết quả đăng ký
     */
    public FaceRegistrationResult registerFaceForVisitor(String faceImageBase64, String visitorId) {
        try {
            if (faceImageBase64 == null || faceImageBase64.trim().isEmpty()) {
                return new FaceRegistrationResult(false, "Ảnh khuôn mặt không được để trống", null, null);
            }

            if (visitorId == null || visitorId.trim().isEmpty()) {
                return new FaceRegistrationResult(false, "ID xe vãng lai không được để trống", null, null);
            }

            logger.info("Đang đăng ký khuôn mặt cho xe vãng lai: {}", visitorId);

            // Gọi FastAPI để đăng ký khuôn mặt
            Map<String, Object> response = faceRecognitionService.registerFace(
                    visitorId, // name
                    faceImageBase64, // base64Image
                    "Xe vãng lai - " + visitorId // description
            );

            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                String faceId = extractFaceIdFromResponse(response);
                BigDecimal similarity = extractSimilarityFromResponse(response);

                if (faceId != null) {
                    logger.info("Đăng ký khuôn mặt thành công cho xe vãng lai - ID: {}, Face ID: {}",
                            visitorId, faceId);
                    return new FaceRegistrationResult(true, "Đăng ký khuôn mặt thành công", faceId, similarity);
                } else {
                    return new FaceRegistrationResult(false, "Không thể tạo Face ID", null, null);
                }
            } else {
                String errorMessage = (String) response.get("message");
                return new FaceRegistrationResult(false, "Đăng ký khuôn mặt thất bại: " + errorMessage, null, null);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi đăng ký khuôn mặt cho xe vãng lai: {}", visitorId, e);
            return new FaceRegistrationResult(false, "Lỗi hệ thống: " + e.getMessage(), null, null);
        }
    }

    /**
     * Nhận diện khuôn mặt khi xe vào (cho xe có đăng ký tháng)
     * 
     * @param faceImageBase64 Ảnh khuôn mặt dạng base64
     * @param threshold       Ngưỡng độ tương đồng (0.0 - 1.0)
     * @return Kết quả nhận diện
     */
    public FaceRecognitionResult recognizeFaceForEntry(String faceImageBase64, Double threshold) {
        try {
            if (faceImageBase64 == null || faceImageBase64.trim().isEmpty()) {
                return new FaceRecognitionResult(false, "Ảnh khuôn mặt không được để trống", null, null);
            }

            if (threshold == null) {
                threshold = 0.6; // Default threshold
            }

            // Gọi FastAPI để nhận diện khuôn mặt
            Map<String, Object> response = faceRecognitionService.recognizeFace(faceImageBase64, threshold);

            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                String faceId = extractFaceIdFromResponse(response);
                BigDecimal similarity = extractSimilarityFromResponse(response);

                if (faceId != null && similarity != null && similarity.doubleValue() >= threshold) {
                    logger.info("Nhận diện khuôn mặt thành công khi xe vào - Face ID: {}, Similarity: {}",
                            faceId, similarity);
                    return new FaceRecognitionResult(true, "Nhận diện khuôn mặt thành công", faceId, similarity);
                } else {
                    return new FaceRecognitionResult(false, "Khuôn mặt không khớp hoặc độ tương đồng thấp", faceId,
                            similarity);
                }
            } else {
                String errorMessage = (String) response.get("message");
                return new FaceRecognitionResult(false, "Nhận diện khuôn mặt thất bại: " + errorMessage, null, null);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi nhận diện khuôn mặt xe vào", e);
            return new FaceRecognitionResult(false, "Lỗi hệ thống: " + e.getMessage(), null, null);
        }
    }

    /**
     * Nhận diện và so sánh khuôn mặt cho xe vãng lai khi ra
     * 
     * @param faceImageBase64 Ảnh khuôn mặt dạng base64
     * @param expectedFaceId  Face ID từ lúc xe vào
     * @param threshold       Ngưỡng độ tương đồng (0.0 - 1.0)
     * @return Kết quả xác thực
     */
    public FaceVerificationResult verifyFaceForVisitorExit(String faceImageBase64, String expectedFaceId,
            Double threshold) {
        try {
            if (faceImageBase64 == null || faceImageBase64.trim().isEmpty()) {
                return new FaceVerificationResult(false,
                        "Ảnh khuôn mặt không được để trống",
                        ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
            }

            if (expectedFaceId == null || expectedFaceId.trim().isEmpty()) {
                return new FaceVerificationResult(false,
                        "Không tìm thấy thông tin khuôn mặt khi xe vào",
                        ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
            }

            if (threshold == null) {
                threshold = 0.6; // Default threshold
            }

            logger.info("Đang xác thực khuôn mặt xe vãng lai ra - Expected Face ID: {}", expectedFaceId);

            // Gọi FastAPI để nhận diện khuôn mặt
            Map<String, Object> response = faceRecognitionService.recognizeFace(faceImageBase64, threshold);

            Boolean success = (Boolean) response.get("success");
            if (success != null && success) {
                String recognizedFaceId = extractFaceIdFromResponse(response);
                BigDecimal similarity = extractSimilarityFromResponse(response);

                if (recognizedFaceId != null && expectedFaceId.equals(recognizedFaceId)) {
                    logger.info("Xác thực khuôn mặt xe vãng lai thành công - Face ID khớp: {}, Similarity: {}",
                            recognizedFaceId, similarity);
                    return new FaceVerificationResult(true,
                            "Xác thực khuôn mặt thành công - Khuôn mặt khớp với lúc xe vào",
                            ParkingTransaction.FaceVerificationStatus.VERIFIED_BOTH);
                } else {
                    logger.warn("Xác thực khuôn mặt xe vãng lai thất bại - Face ID không khớp. Expected: {}, Got: {}",
                            expectedFaceId, recognizedFaceId);
                    return new FaceVerificationResult(false,
                            "Khuôn mặt không khớp với lúc xe vào (Expected: " + expectedFaceId + ", Got: "
                                    + recognizedFaceId + ")",
                            ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
                }
            } else {
                String errorMessage = (String) response.get("message");
                return new FaceVerificationResult(false,
                        "Nhận diện khuôn mặt thất bại: " + errorMessage,
                        ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xác thực khuôn mặt xe vãng lai ra", e);
            return new FaceVerificationResult(false,
                    "Lỗi hệ thống: " + e.getMessage(),
                    ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
        }
    }

    /**
     * Nhận diện khuôn mặt khi xe ra (method cũ)
     * 
     * @param faceImageBase64 Ảnh khuôn mặt dạng base64
     * @param threshold       Ngưỡng độ tương đồng (0.0 - 1.0)
     * @return Kết quả nhận diện
     */
    public FaceRecognitionResult recognizeFaceForExit(String faceImageBase64, Double threshold) {
        // Logic tương tự như recognizeFaceForEntry
        return recognizeFaceForEntry(faceImageBase64, threshold);
    }

    /**
     * Xác thực khuôn mặt cho xe có đăng ký tháng
     * 
     * @param dangKyThang      Thông tin đăng ký tháng
     * @param recognizedFaceId Face ID nhận diện được
     * @param similarity       Độ tương đồng
     * @return Kết quả xác thực
     */
    public FaceVerificationResult verifyFaceForMonthlyRegistration(DangKyThang dangKyThang,
            String recognizedFaceId,
            BigDecimal similarity) {
        try {
            if (dangKyThang.getFaceId() == null) {
                return new FaceVerificationResult(false,
                        "Đăng ký tháng chưa có thông tin khuôn mặt",
                        ParkingTransaction.FaceVerificationStatus.NOT_VERIFIED);
            }

            if (recognizedFaceId == null) {
                return new FaceVerificationResult(false,
                        "Không nhận diện được khuôn mặt",
                        ParkingTransaction.FaceVerificationStatus.FAILED_ENTRY);
            }

            // So sánh Face ID
            if (dangKyThang.getFaceId().equals(recognizedFaceId)) {
                logger.info("Xác thực khuôn mặt thành công cho đăng ký tháng - CCCD: {}, Face ID: {}",
                        dangKyThang.getCccd(), recognizedFaceId);
                return new FaceVerificationResult(true,
                        "Xác thực khuôn mặt thành công",
                        ParkingTransaction.FaceVerificationStatus.VERIFIED_ENTRY);
            } else {
                logger.warn("Xác thực khuôn mặt thất bại - Face ID không khớp. Expected: {}, Got: {}",
                        dangKyThang.getFaceId(), recognizedFaceId);
                return new FaceVerificationResult(false,
                        "Khuôn mặt không khớp với đăng ký tháng",
                        ParkingTransaction.FaceVerificationStatus.FAILED_ENTRY);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xác thực khuôn mặt cho đăng ký tháng", e);
            return new FaceVerificationResult(false,
                    "Lỗi hệ thống: " + e.getMessage(),
                    ParkingTransaction.FaceVerificationStatus.FAILED_ENTRY);
        }
    }

    /**
     * Xác thực khuôn mặt khi xe ra cho khách vãng lai
     * 
     * @param transaction      Giao dịch đỗ xe
     * @param recognizedFaceId Face ID nhận diện được khi xe ra
     * @param similarity       Độ tương đồng
     * @return Kết quả xác thực
     */
    public FaceVerificationResult verifyFaceForExitVisitor(ParkingTransaction transaction,
            String recognizedFaceId,
            BigDecimal similarity) {
        try {
            if (transaction.getFaceIdEntry() == null) {
                return new FaceVerificationResult(false,
                        "Không có thông tin khuôn mặt khi xe vào",
                        ParkingTransaction.FaceVerificationStatus.NOT_VERIFIED);
            }

            if (recognizedFaceId == null) {
                return new FaceVerificationResult(false,
                        "Không nhận diện được khuôn mặt khi xe ra",
                        ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
            }

            // So sánh Face ID vào và ra
            if (transaction.getFaceIdEntry().equals(recognizedFaceId)) {
                logger.info("Xác thực khuôn mặt xe ra thành công cho khách vãng lai - Biển số: {}, Face ID: {}",
                        transaction.getBienSoXe(), recognizedFaceId);
                return new FaceVerificationResult(true,
                        "Xác thực khuôn mặt xe ra thành công",
                        ParkingTransaction.FaceVerificationStatus.VERIFIED_EXIT);
            } else {
                logger.warn("Xác thực khuôn mặt xe ra thất bại - Face ID không khớp. Entry: {}, Exit: {}",
                        transaction.getFaceIdEntry(), recognizedFaceId);
                return new FaceVerificationResult(false,
                        "Khuôn mặt xe ra không khớp với xe vào",
                        ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
            }

        } catch (Exception e) {
            logger.error("Lỗi khi xác thực khuôn mặt xe ra cho khách vãng lai", e);
            return new FaceVerificationResult(false,
                    "Lỗi hệ thống: " + e.getMessage(),
                    ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
        }
    }

    /**
     * Trích xuất Face ID từ response của FastAPI
     */
    private String extractFaceIdFromResponse(Map<String, Object> response) {
        try {
            System.out.println(" [FaceService] Extracting Face ID from response: " + response);

            // First, try to get face_id directly from root level
            Object faceIdObj = response.get("face_id");
            if (faceIdObj != null) {
                System.out.println(" [FaceService] Found face_id at root level: " + faceIdObj.toString());
                return faceIdObj.toString();
            }

            // If not found at root, try to get from data object
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null) {
                Object dataFaceIdObj = data.get("face_id");
                if (dataFaceIdObj != null) {
                    System.out.println(" [FaceService] Found face_id in data object: " + dataFaceIdObj.toString());
                    return dataFaceIdObj.toString();
                }
            }

            System.out.println(" [FaceService] No face_id found in response");
            return null;
        } catch (Exception e) {
            System.out.println(" [FaceService] Exception extracting Face ID: " + e.getMessage());
            logger.warn("Không thể trích xuất Face ID từ response", e);
            return null;
        }
    }

    /**
     * Trích xuất Similarity từ response của FastAPI
     */
    private BigDecimal extractSimilarityFromResponse(Map<String, Object> response) {
        try {
            System.out.println(" [FaceService] Extracting Similarity from response");

            // First, try to get similarity directly from root level
            Object similarityObj = response.get("similarity");
            if (similarityObj != null) {
                System.out.println(" [FaceService] Found similarity at root level: " + similarityObj.toString());
                return BigDecimal.valueOf(Double.parseDouble(similarityObj.toString()));
            }

            // If not found at root, try to get from data object
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) response.get("data");
            if (data != null) {
                Object dataSimilarityObj = data.get("similarity");
                if (dataSimilarityObj != null) {
                    System.out.println(
                            " [FaceService] Found similarity in data object: " + dataSimilarityObj.toString());
                    return BigDecimal.valueOf(Double.parseDouble(dataSimilarityObj.toString()));
                }
            }

            System.out.println(" [FaceService] No similarity found in response, using default 0.0");
            return BigDecimal.ZERO; // Default similarity if not found
        } catch (Exception e) {
            System.out.println(" [FaceService] Exception extracting Similarity: " + e.getMessage());
            logger.warn("Không thể trích xuất Similarity từ response", e);
            return BigDecimal.ZERO;
        }
    }

    // Inner Classes cho kết quả trả về

    public static class FaceRegistrationResult {
        private boolean success;
        private String message;
        private String faceId;
        private BigDecimal similarity;

        public FaceRegistrationResult(boolean success, String message, String faceId, BigDecimal similarity) {
            this.success = success;
            this.message = message;
            this.faceId = faceId;
            this.similarity = similarity;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getFaceId() {
            return faceId;
        }

        public BigDecimal getSimilarity() {
            return similarity;
        }
    }

    public static class FaceRecognitionResult {
        private boolean success;
        private String message;
        private String faceId;
        private BigDecimal similarity;

        public FaceRecognitionResult(boolean success, String message, String faceId, BigDecimal similarity) {
            this.success = success;
            this.message = message;
            this.faceId = faceId;
            this.similarity = similarity;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getFaceId() {
            return faceId;
        }

        public BigDecimal getSimilarity() {
            return similarity;
        }
    }

    public static class FaceVerificationResult {
        private boolean success;
        private String message;
        private ParkingTransaction.FaceVerificationStatus status;

        public FaceVerificationResult(boolean success, String message,
                ParkingTransaction.FaceVerificationStatus status) {
            this.success = success;
            this.message = message;
            this.status = status;
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public ParkingTransaction.FaceVerificationStatus getStatus() {
            return status;
        }
    }
}