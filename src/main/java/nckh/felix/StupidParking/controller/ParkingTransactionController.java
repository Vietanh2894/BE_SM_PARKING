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
     * Tạo yêu cầu xe vào bãi đỗ
     */
    @PostMapping("/entry-request")
    public ResponseEntity<?> createEntryRequest(@RequestBody Map<String, String> request) {
        try {
            String bienSoXe = request.get("bienSoXe");
            String maBaiDo = request.get("maBaiDo");
            String maLoaiXe = request.get("maLoaiXe");
            String ghiChu = request.get("ghiChu");

            ParkingTransaction transaction = parkingTransactionService.createEntryRequest(
                    bienSoXe, maBaiDo, maLoaiXe, ghiChu);

            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
     * Duyệt xe vào bãi đỗ
     */
    @PostMapping("/{maGiaoDich}/approve-entry")
    public ResponseEntity<?> approveEntry(@PathVariable("maGiaoDich") Long maGiaoDich) {
        try {
            // Lấy thông tin nhân viên từ token
            String currentStaffUsername = getCurrentStaffUsername();
            Staff staff = staffService.fetchStaffByUsername(currentStaffUsername);

            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
            }

            ParkingTransaction transaction = parkingTransactionService.approveEntry(maGiaoDich, staff.getMaNV());
            return ResponseEntity.ok(transaction);
        } catch (IdInvalidException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Tạo yêu cầu xe ra
     */
    @PostMapping("/exit-request")
    public ResponseEntity<?> createExitRequest(@RequestBody Map<String, String> request) {
        try {
            String bienSoXe = request.get("bienSoXe");

            ParkingTransaction transaction = parkingTransactionService.createExitRequest(bienSoXe);
            return ResponseEntity.ok(transaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Duyệt xe ra và thanh toán
     */
    @PostMapping("/{maGiaoDich}/approve-exit")
    public ResponseEntity<?> approveExit(@PathVariable("maGiaoDich") Long maGiaoDich,
            @RequestBody Map<String, String> request) {
        try {
            // Lấy thông tin nhân viên từ token
            String currentStaffUsername = getCurrentStaffUsername();
            Staff staff = staffService.fetchStaffByUsername(currentStaffUsername);

            if (staff == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy thông tin nhân viên"));
            }

            String soTienStr = request.get("soTienThanhToan");
            BigDecimal soTienThanhToan = soTienStr != null ? new BigDecimal(soTienStr) : null;

            ParkingTransaction transaction = parkingTransactionService.approveExit(
                    maGiaoDich, staff.getMaNV(), soTienThanhToan);

            return ResponseEntity.ok(transaction);
        } catch (IdInvalidException | IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
     * Lấy danh sách giao dịch chờ duyệt vào
     */
    @GetMapping("/pending-in")
    public ResponseEntity<List<ParkingTransaction>> getPendingInTransactions() {
        return ResponseEntity.ok(parkingTransactionService.fetchPendingInTransactions());
    }

    /**
     * Lấy danh sách giao dịch chờ duyệt ra
     */
    @GetMapping("/pending-out")
    public ResponseEntity<List<ParkingTransaction>> getPendingOutTransactions() {
        return ResponseEntity.ok(parkingTransactionService.fetchPendingOutTransactions());
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
