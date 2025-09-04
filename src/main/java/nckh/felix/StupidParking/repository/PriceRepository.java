package nckh.felix.StupidParking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.Price;
import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.domain.ParkingMode;

@Repository
public interface PriceRepository extends JpaRepository<Price, String> {
    // JpaRepository sẽ tự động cung cấp các phương thức CRUD với String làm ID
    // findById, save, delete, findAll, etc.

    // Tìm giá theo loại xe
    List<Price> findByMaLoaiXe(VehicleType maLoaiXe);

    // Tìm giá theo hình thức đỗ xe
    List<Price> findByMaHinhThuc(ParkingMode maHinhThuc);

    // Tìm giá theo loại xe và hình thức đỗ xe
    List<Price> findByMaLoaiXeAndMaHinhThuc(VehicleType maLoaiXe, ParkingMode maHinhThuc);
}
