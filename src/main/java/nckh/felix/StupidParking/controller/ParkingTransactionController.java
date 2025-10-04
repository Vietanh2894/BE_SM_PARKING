package nckh.felix.StupidParking.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import nckh.felix.StupidParking.domain.ParkingTransaction;
import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.service.ParkingTransactionService;
import nckh.felix.StupidParking.service.StaffService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

@RestController
@RequestMapping("/parking-transactions")
public class ParkingTransactionController {

    private final ParkingTransactionService parkingTransactionService;
    private final StaffService staffService;

    public ParkingTransactionController(ParkingTransactionService parkingTransactionService,
            StaffService staffService) {
        this.parkingTransactionService = parkingTransactionService;
        this.staffService = staffService;
    }

    /**
     * CHO XE VÀO TRỰC TIẾP VỚI FACE RECOGNITION - DÀNH CHO MOBILE/CAMERA SCAN
     * API kết hợp tạo yêu cầu và duyệt vào trong 1 bước + xác thực khuôn mặt
     */
    @PostMapping("/direct-entry-with-face")
    public ResponseEntity<?> directVehicleEntryWithFace(@RequestBody Map<String, String> request) {
        try {
            String bienSoXe = request.get("bienSoXe");
            String maBaiDo = request.get("maBaiDo");
            String maLoaiXe = request.get("maLoaiXe");
            String ghiChu = request.get("ghiChu");
            String faceImageBase64 = request.get("faceImageBase64"); // Ảnh khuôn mặt dạng base64

            // Lấy thông tin nhân viên từ token
            String currentStaffUsername = getCurrentStaffUsername();
            Staff staff = staffService.fetchStaffByUsername(currentStaffUsername);

            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
            }

            ParkingTransaction transaction = parkingTransactionService.directVehicleEntryWithFace(
                    bienSoXe, maBaiDo, maLoaiXe, staff.getMaNV(), ghiChu, faceImageBase64);

            // Prepare response data with null checks
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Xe đã được cho vào bãi đỗ với xác thực khuôn mặt thành công");
            responseData.put("transaction", transaction);
            responseData.put("faceVerificationStatus",
                    transaction.getFaceVerificationStatus() != null ? transaction.getFaceVerificationStatus().toString()
                            : "NOT_VERIFIED");
            responseData.put("faceSimilarityEntry",
                    transaction.getFaceSimilarityEntry() != null ? transaction.getFaceSimilarityEntry() : "N/A");

            return ResponseEntity.status(HttpStatus.CREATED).body(responseData);
        } catch (IdInvalidException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * CHO XE VÀO TRỰC TIẾP - DÀNH CHO MOBILE/CAMERA SCAN
     * API kết hợp tạo yêu cầu và duyệt vào trong 1 bước
     */
    @PostMapping("/direct-entry")
    public ResponseEntity<?> directVehicleEntry(@RequestBody Map<String, String> request) {
        try {
            String bienSoXe = request.get("bienSoXe");
            String maBaiDo = request.get("maBaiDo");
            String maLoaiXe = request.get("maLoaiXe");
            String ghiChu = request.get("ghiChu");

            // Lấy thông tin nhân viên từ token
            String currentStaffUsername = getCurrentStaffUsername();
            Staff staff = staffService.fetchStaffByUsername(currentStaffUsername);

            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
            }

            ParkingTransaction transaction = parkingTransactionService.directVehicleEntry(
                    bienSoXe, maBaiDo, maLoaiXe, staff.getMaNV(), ghiChu);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Xe đã được cho vào bãi đỗ thành công",
                    "transaction", transaction));
        } catch (IdInvalidException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * KIỂM TRA TRẠNG THÁI XE KHI QUÉT BIỂN SỐ
     * API để kiểm tra xe có đăng ký tháng không và có thể vào bãi không
     */
    @GetMapping("/check-vehicle-status/{bienSoXe}")
    public ResponseEntity<?> checkVehicleStatus(@PathVariable("bienSoXe") String bienSoXe) {
        try {
            ParkingTransactionService.VehicleEntryStatus status = parkingTransactionService
                    .checkVehicleEntryStatus(bienSoXe);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "bienSoXe", status.getBienSoXe(),
                    "hasActiveMonthlyRegistration", status.isHasActiveMonthlyRegistration(),
                    "isCurrentlyParked", status.isCurrentlyParked(),
                    "canEnter", status.canEnter(),
                    "message", status.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * CHO XE RA TRỰC TIẾP VỚI FACE RECOGNITION - DÀNH CHO MOBILE/CAMERA SCAN
     * API kết hợp tạo yêu cầu và duyệt ra trong 1 bước + xác thực khuôn mặt
     */
    @PostMapping("/direct-exit-with-face")
    public ResponseEntity<?> directVehicleExitWithFace(@RequestBody Map<String, String> request) {
        try {
            String bienSoXe = request.get("bienSoXe");
            String soTienStr = request.get("soTienThanhToan");
            String faceImageBase64 = request.get("faceImageBase64"); // Ảnh khuôn mặt dạng base64

            // Lấy thông tin nhân viên từ token
            String currentStaffUsername = getCurrentStaffUsername();
            Staff staff = staffService.fetchStaffByUsername(currentStaffUsername);

            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
            }

            // Parse số tiền thanh toán (có thể null để tính tự động)
            BigDecimal soTienThanhToan = soTienStr != null && !soTienStr.trim().isEmpty()
                    ? new BigDecimal(soTienStr)
                    : null;

            ParkingTransaction transaction = parkingTransactionService.directVehicleExitWithFace(
                    bienSoXe, staff.getMaNV(), soTienThanhToan, faceImageBase64);

            // Prepare response data with null checks
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("success", true);
            responseData.put("message", "Xe đã được cho ra bãi đỗ với xác thực khuôn mặt thành công");
            responseData.put("transaction", transaction);
            responseData.put("faceVerificationStatus",
                    transaction.getFaceVerificationStatus() != null ? transaction.getFaceVerificationStatus().toString()
                            : "NOT_VERIFIED");
            responseData.put("faceSimilarityExit",
                    transaction.getFaceSimilarityExit() != null ? transaction.getFaceSimilarityExit() : "N/A");
            responseData.put("soTienThanhToan",
                    transaction.getSoTienThanhToan() != null ? transaction.getSoTienThanhToan() : BigDecimal.ZERO);

            return ResponseEntity.ok(responseData);
        } catch (IdInvalidException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * CHO XE RA TRỰC TIẾP - DÀNH CHO MOBILE/CAMERA SCAN
     * API kết hợp tạo yêu cầu và duyệt ra trong 1 bước
     */
    @PostMapping("/direct-exit")
    public ResponseEntity<?> directVehicleExit(@RequestBody Map<String, String> request) {
        try {
            String bienSoXe = request.get("bienSoXe");
            String soTienStr = request.get("soTienThanhToan");

            // Lấy thông tin nhân viên từ token
            String currentStaffUsername = getCurrentStaffUsername();
            Staff staff = staffService.fetchStaffByUsername(currentStaffUsername);

            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
            }

            // Parse số tiền thanh toán (có thể null để tính tự động)
            BigDecimal soTienThanhToan = soTienStr != null && !soTienStr.trim().isEmpty()
                    ? new BigDecimal(soTienStr)
                    : null;

            ParkingTransaction transaction = parkingTransactionService.directVehicleExit(
                    bienSoXe, staff.getMaNV(), soTienThanhToan);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xe đã được cho ra bãi đỗ và thanh toán thành công",
                    "transaction", transaction,
                    "soTienThanhToan", transaction.getSoTienThanhToan()));
        } catch (IdInvalidException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()));
        }
    }

    /**
     * Tính tiền đỗ xe tự động
     */
    @GetMapping("/{maGiaoDich}/calculate-fee")
    public ResponseEntity<?> calculateParkingFee(@PathVariable("maGiaoDich") Long maGiaoDich) {
        try {
            BigDecimal fee = parkingTransactionService.calculateParkingFee(maGiaoDich);
            return ResponseEntity.ok(Map.of("soTienThanhToan", fee));
        } catch (IdInvalidException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Hủy giao dịch
     */
    @PostMapping("/{maGiaoDich}/cancel")
    public ResponseEntity<?> cancelTransaction(@PathVariable("maGiaoDich") Long maGiaoDich) {
        try {
            // Lấy thông tin nhân viên từ token
            String currentStaffUsername = getCurrentStaffUsername();
            Staff staff = staffService.fetchStaffByUsername(currentStaffUsername);

            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
            }

            ParkingTransaction transaction = parkingTransactionService.cancelTransaction(maGiaoDich, staff.getMaNV());
            return ResponseEntity.ok(transaction);
        } catch (IdInvalidException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy thông tin giao dịch theo ID
     */
    @GetMapping("/{maGiaoDich}")
    public ResponseEntity<?> getTransactionById(@PathVariable("maGiaoDich") Long maGiaoDich) {
        try {
            ParkingTransaction transaction = parkingTransactionService.fetchTransactionById(maGiaoDich);
            return ResponseEntity.ok(transaction);
        } catch (IdInvalidException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy tất cả giao dịch
     */
    @GetMapping
    public ResponseEntity<List<ParkingTransaction>> getAllTransactions() {
        return ResponseEntity.ok(parkingTransactionService.fetchAllTransactions());
    }

    /**
     * Lấy lịch sử giao dịch theo biển số xe
     */
    @GetMapping("/vehicle/{bienSoXe}")
    public ResponseEntity<List<ParkingTransaction>> getTransactionsByVehicle(
            @PathVariable("bienSoXe") String bienSoXe) {
        return ResponseEntity.ok(parkingTransactionService.fetchTransactionsByBienSoXe(bienSoXe));
    }

    /**
     * Kiểm tra xe có đang đỗ trong bãi không
     */
    @GetMapping("/vehicle/{bienSoXe}/status")
    public ResponseEntity<Map<String, Object>> getVehicleStatus(@PathVariable("bienSoXe") String bienSoXe) {
        boolean isParked = parkingTransactionService.isVehicleCurrentlyParked(bienSoXe);
        Map<String, Object> response = new HashMap<>();
        response.put("bienSoXe", bienSoXe);
        response.put("isCurrentlyParked", isParked);

        if (isParked) {
            parkingTransactionService.fetchActiveTransactionByBienSoXe(bienSoXe)
                    .ifPresent(transaction -> {
                        response.put("activeTransaction", transaction);
                    });
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy giao dịch hoàn thành trong ngày
     */
    @GetMapping("/completed/today")
    public ResponseEntity<List<ParkingTransaction>> getTodayCompletedTransactions() {
        LocalDateTime today = LocalDateTime.now();
        return ResponseEntity.ok(parkingTransactionService.fetchCompletedTransactionsByDate(today));
    }

    /**
     * Thống kê doanh thu theo khoảng thời gian
     * Ví dụ: GET
     * /parking-transactions/statistics/revenue?startDate=2025-09-01&endDate=2025-09-30
     * Hoặc không cần parameter sẽ lấy tháng hiện tại
     */
    @GetMapping("/statistics/revenue")
    public ResponseEntity<List<Object[]>> getRevenueStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Nếu không có parameter, lấy tháng hiện tại
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1); // Ngày đầu tháng
            endDate = now.withDayOfMonth(now.lengthOfMonth()); // Ngày cuối tháng
        }

        // Chuyển đổi LocalDate thành LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 23:59:59

        return ResponseEntity.ok(parkingTransactionService.getRevenueByDateRange(startDateTime, endDateTime));
    }

    /**
     * Thống kê số lượt xe theo loại trong khoảng thời gian
     * Ví dụ: GET
     * /parking-transactions/statistics/vehicle-count?startDate=2025-09-01&endDate=2025-09-30
     * Hoặc không cần parameter sẽ lấy tháng hiện tại
     */
    @GetMapping("/statistics/vehicle-count")
    public ResponseEntity<List<Object[]>> getVehicleCountStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Nếu không có parameter, lấy tháng hiện tại
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1); // Ngày đầu tháng
            endDate = now.withDayOfMonth(now.lengthOfMonth()); // Ngày cuối tháng
        }

        // Chuyển đổi LocalDate thành LocalDateTime
        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 23:59:59

        return ResponseEntity
                .ok(parkingTransactionService.getVehicleCountByTypeAndDateRange(startDateTime, endDateTime));
    }

    /**
     * Đếm số xe đang đỗ trong bãi
     */
    @GetMapping("/count/parking-lot/{maBaiDo}")
    public ResponseEntity<Map<String, Object>> countActiveTransactionsByParkingLot(
            @PathVariable("maBaiDo") String maBaiDo) {
        Long count = parkingTransactionService.countActiveTransactionsByParkingLot(maBaiDo);
        return ResponseEntity.ok(Map.of("maBaiDo", maBaiDo, "activeTransactionsCount", count));
    }

    // Helper method để lấy username của staff hiện tại
    private String getCurrentStaffUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
