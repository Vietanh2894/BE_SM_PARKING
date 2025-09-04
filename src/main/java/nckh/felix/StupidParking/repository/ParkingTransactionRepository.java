package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.ParkingTransaction;
import nckh.felix.StupidParking.domain.ParkingTransaction.TrangThaiGiaoDich;
import nckh.felix.StupidParking.domain.ParkingLot;
import nckh.felix.StupidParking.domain.VehicleType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParkingTransactionRepository extends JpaRepository<ParkingTransaction, Long> {

        // Tìm giao dịch theo biển số xe và trạng thái
        List<ParkingTransaction> findByBienSoXeAndTrangThai(String bienSoXe, TrangThaiGiaoDich trangThai);

        // Tìm giao dịch đang hoạt động của một xe (đã vào chưa ra)
        @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.bienSoXe = :bienSoXe AND pt.trangThai = 1")
        Optional<ParkingTransaction> findActiveTransactionByBienSoXe(@Param("bienSoXe") String bienSoXe);

        // Tìm tất cả giao dịch chờ duyệt vào
        List<ParkingTransaction> findByTrangThaiOrderByCreatedDateAsc(TrangThaiGiaoDich trangThai);

        // Tìm giao dịch theo bãi đỗ và trạng thái
        List<ParkingTransaction> findByParkingLotAndTrangThai(ParkingLot parkingLot, TrangThaiGiaoDich trangThai);

        // Tìm giao dịch theo khoảng thời gian
        @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.thoiGianVao BETWEEN :startTime AND :endTime")
        List<ParkingTransaction> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Tìm giao dịch đã hoàn thành trong ngày
        @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.trangThai = 3 AND DATE(pt.thoiGianRa) = DATE(:date)")
        List<ParkingTransaction> findCompletedTransactionsByDate(@Param("date") LocalDateTime date);

        // Đếm số xe đang đỗ trong bãi
        @Query("SELECT COUNT(pt) FROM ParkingTransaction pt WHERE pt.parkingLot = :parkingLot AND pt.trangThai = 1")
        Long countActiveTransactionsByParkingLot(@Param("parkingLot") ParkingLot parkingLot);

        // Thống kê doanh thu theo ngày
        @Query("SELECT DATE(pt.thoiGianRa), SUM(pt.soTienThanhToan) FROM ParkingTransaction pt " +
                        "WHERE pt.trangThai = 3 AND pt.thoiGianRa BETWEEN :startDate AND :endDate " +
                        "GROUP BY DATE(pt.thoiGianRa) ORDER BY DATE(pt.thoiGianRa)")
        List<Object[]> getRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Thống kê số lượt xe theo loại xe
        @Query("SELECT pt.vehicleType, COUNT(pt) FROM ParkingTransaction pt " +
                        "WHERE pt.trangThai = 3 AND pt.thoiGianRa BETWEEN :startDate AND :endDate " +
                        "GROUP BY pt.vehicleType")
        List<Object[]> getVehicleCountByTypeAndDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tìm giao dịch theo biển số xe trong khoảng thời gian
        @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.bienSoXe = :bienSoXe " +
                        "AND pt.thoiGianVao BETWEEN :startTime AND :endTime ORDER BY pt.thoiGianVao DESC")
        List<ParkingTransaction> findByBienSoXeAndTimeRange(@Param("bienSoXe") String bienSoXe,
                        @Param("startTime") LocalDateTime startTime,
                        @Param("endTime") LocalDateTime endTime);

        // Tìm giao dịch chờ duyệt ra
        @Query("SELECT pt FROM ParkingTransaction pt WHERE pt.trangThai = 2 ORDER BY pt.thoiGianRa ASC")
        List<ParkingTransaction> findPendingOutTransactions();

        // Kiểm tra xe có đang đỗ trong bãi không
        @Query(value = "SELECT COUNT(*) FROM parking_transactions WHERE bien_so_xe = :bienSoXe AND trang_thai = 1", nativeQuery = true)
        Long countVehicleCurrentlyParked(@Param("bienSoXe") String bienSoXe);
}