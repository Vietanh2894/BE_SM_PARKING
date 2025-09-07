package nckh.felix.StupidParking.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import nckh.felix.StupidParking.service.ParkingTransactionService;
import nckh.felix.StupidParking.service.ParkingLotService;

@RestController
@RequestMapping("/integrated-parking")
public class IntegratedParkingController {

    private final ParkingTransactionService parkingTransactionService;
    private final ParkingLotService parkingLotService;

    public IntegratedParkingController(ParkingTransactionService parkingTransactionService,
            ParkingLotService parkingLotService) {
        this.parkingTransactionService = parkingTransactionService;
        this.parkingLotService = parkingLotService;
    }

    /**
     * Dashboard tổng quan hệ thống đỗ xe
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = Map.of(
                "totalParkingLots", parkingLotService.fetchAllParkingLots().size(),
                "activeParkingLots", parkingLotService.fetchActiveParkingLots().size(),
                "availableParkingLots", parkingLotService.fetchAvailableParkingLots().size(),
                "pendingInTransactions", parkingTransactionService.fetchPendingInTransactions().size(),
                "pendingOutTransactions", parkingTransactionService.fetchPendingOutTransactions().size(),
                "completedTransactionsToday",
                parkingTransactionService.fetchCompletedTransactionsByDate(LocalDateTime.now()).size());

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Thống kê chi tiết theo bãi đỗ
     */
    @GetMapping("/parking-lot/{maBaiDo}/statistics")
    public ResponseEntity<Map<String, Object>> getParkingLotStatistics(@PathVariable("maBaiDo") String maBaiDo) {
        var parkingLot = parkingLotService.fetchParkingLotByMaBaiDo(maBaiDo);
        if (parkingLot == null) {
            return ResponseEntity.notFound().build();
        }

        Long activeTransactions = parkingTransactionService.countActiveTransactionsByParkingLot(maBaiDo);

        Map<String, Object> statistics = Map.of(
                "parkingLot", parkingLot,
                "currentOccupancy", activeTransactions,
                "availableSpaces", parkingLot.getSoChoTrong(),
                "totalSpaces", parkingLot.getTongSoCho(),
                "occupancyRate", parkingLot.getOccupancyRate());

        return ResponseEntity.ok(statistics);
    }

    /**
     * Báo cáo tổng hợp theo khoảng thời gian
     */
    @GetMapping("/reports/summary")
    public ResponseEntity<Map<String, Object>> getSummaryReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        List<Object[]> revenueData = parkingTransactionService.getRevenueByDateRange(startDate, endDate);
        List<Object[]> vehicleCountData = parkingTransactionService.getVehicleCountByTypeAndDateRange(startDate,
                endDate);

        Map<String, Object> report = Map.of(
                "period", Map.of("startDate", startDate, "endDate", endDate),
                "revenueByDate", revenueData,
                "vehicleCountByType", vehicleCountData,
                "parkingLotCapacity", parkingLotService.getTotalCapacityByVehicleType(),
                "availableSpaces", parkingLotService.getAvailableSpacesByVehicleType());

        return ResponseEntity.ok(report);
    }

    /**
     * Lấy trạng thái realtime của tất cả bãi đỗ
     */
    @GetMapping("/realtime-status")
    public ResponseEntity<List<Map<String, Object>>> getRealtimeStatus() {
        var parkingLots = parkingLotService.fetchActiveParkingLots();

        List<Map<String, Object>> statusList = parkingLots.stream()
                .map(lot -> {
                    Long activeCount = parkingTransactionService.countActiveTransactionsByParkingLot(lot.getMaBaiDo());
                    Map<String, Object> status = new HashMap<>();
                    status.put("maBaiDo", lot.getMaBaiDo());
                    status.put("tenBaiDo", lot.getTenBaiDo());
                    status.put("loaiXe", lot.getMaLoaiXe().getTenLoaiXe());
                    status.put("soChoTrong", lot.getSoChoTrong());
                    status.put("tongSoCho", lot.getTongSoCho());
                    status.put("soXeDangDo", activeCount);
                    status.put("trangThai", lot.getTrangThai());
                    status.put("occupancyRate", lot.getOccupancyRate());
                    return status;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(statusList);
    }

    /**
     * Tìm kiếm bãi đỗ phù hợp cho loại xe
     */
    @GetMapping("/find-parking")
    public ResponseEntity<List<Map<String, Object>>> findSuitableParkingLots(
            @RequestParam String maLoaiXe,
            @RequestParam(required = false) String diaChi) {

        var vehicleType = new nckh.felix.StupidParking.domain.VehicleType(maLoaiXe);
        var availableLots = parkingLotService.fetchAvailableParkingLotsByVehicleType(vehicleType);

        List<Map<String, Object>> recommendations = availableLots.stream()
                .filter(lot -> diaChi == null || lot.getDiaChi().toLowerCase().contains(diaChi.toLowerCase()))
                .map(lot -> {
                    Map<String, Object> rec = new HashMap<>();
                    rec.put("maBaiDo", lot.getMaBaiDo());
                    rec.put("tenBaiDo", lot.getTenBaiDo());
                    rec.put("diaChi", lot.getDiaChi());
                    rec.put("soChoTrong", lot.getSoChoTrong());
                    rec.put("occupancyRate", lot.getOccupancyRate());
                    rec.put("recommendation", lot.getSoChoTrong() > 5 ? "Highly Available"
                            : lot.getSoChoTrong() > 2 ? "Available" : "Limited Space");
                    return rec;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(recommendations);
    }
}
