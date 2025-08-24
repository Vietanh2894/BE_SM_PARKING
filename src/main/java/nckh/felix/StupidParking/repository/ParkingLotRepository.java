package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.ParkingLot;
import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.domain.ParkingLot.TrangThaiBaiDo;

import java.util.List;

@Repository
public interface ParkingLotRepository extends JpaRepository<ParkingLot, String> {

    // Tìm bãi đỗ theo loại xe
    List<ParkingLot> findByMaLoaiXe(VehicleType maLoaiXe);

    // Tìm bãi đỗ theo trạng thái
    List<ParkingLot> findByTrangThai(TrangThaiBaiDo trangThai);

    // Tìm bãi đỗ có chỗ trống
    @Query("SELECT p FROM ParkingLot p WHERE p.soChoTrong > 0")
    List<ParkingLot> findAvailableParkingLots();

    // Tìm bãi đỗ có chỗ trống theo loại xe
    @Query("SELECT p FROM ParkingLot p WHERE p.soChoTrong > 0 AND p.maLoaiXe = :vehicleType AND p.trangThai = 'ACTIVE'")
    List<ParkingLot> findAvailableParkingLotsByVehicleType(@Param("vehicleType") VehicleType vehicleType);

    // Tìm bãi đỗ theo tên (like search)
    @Query("SELECT p FROM ParkingLot p WHERE p.tenBaiDo LIKE %:tenBaiDo%")
    List<ParkingLot> findByTenBaiDoContaining(@Param("tenBaiDo") String tenBaiDo);

    // Thống kê tổng số chỗ đỗ theo loại xe
    @Query("SELECT p.maLoaiXe, SUM(p.tongSoCho) FROM ParkingLot p GROUP BY p.maLoaiXe")
    List<Object[]> getTotalCapacityByVehicleType();

    // Thống kê số chỗ trống theo loại xe
    @Query("SELECT p.maLoaiXe, SUM(p.soChoTrong) FROM ParkingLot p WHERE p.trangThai = 'ACTIVE' GROUP BY p.maLoaiXe")
    List<Object[]> getAvailableSpacesByVehicleType();
}
