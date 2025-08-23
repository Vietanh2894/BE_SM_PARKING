package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.VehicleType;

@Repository
public interface VehicleTypeRepository extends JpaRepository<VehicleType, String> {
    // JpaRepository sẽ tự động cung cấp các phương thức CRUD với String làm ID (maLoaiXe)
    // findById, save, delete, findAll, etc.
}
