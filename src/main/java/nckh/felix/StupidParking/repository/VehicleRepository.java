package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {
    // JpaRepository sẽ tự động cung cấp các phương thức CRUD với String làm ID
    // findById, save, delete, findAll, etc.
}
