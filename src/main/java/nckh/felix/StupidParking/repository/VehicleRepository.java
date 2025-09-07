package nckh.felix.StupidParking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    // JpaRepository sẽ tự động cung cấp các phương thức CRUD với String làm ID
    // findById, save, delete, findAll, etc.

    // Tìm xe theo email của chủ xe
    @Query("SELECT v FROM Vehicle v WHERE v.owner.email = :email ORDER BY v.createdDate DESC")
    List<Vehicle> findByOwnerEmail(@Param("email") String email);

    // Tìm xe theo CCCD của chủ xe
    @Query("SELECT v FROM Vehicle v WHERE v.owner.cccd = :cccd ORDER BY v.createdDate DESC")
    List<Vehicle> findByOwnerCccd(@Param("cccd") String cccd);

    // Tìm xe theo số điện thoại của chủ xe
    @Query("SELECT v FROM Vehicle v WHERE v.owner.sdt = :sdt ORDER BY v.createdDate DESC")
    List<Vehicle> findByOwnerSdt(@Param("sdt") String sdt);

    // Tìm xe theo User ID
    @Query("SELECT v FROM Vehicle v WHERE v.owner.id = :userId ORDER BY v.createdDate DESC")
    List<Vehicle> findByOwnerId(@Param("userId") Long userId);
}
