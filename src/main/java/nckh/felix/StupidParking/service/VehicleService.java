package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.Vehicle;
import nckh.felix.StupidParking.repository.VehicleRepository;

@Service
public class VehicleService {
    private VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle handleCreateVehicle(Vehicle vehicle) {
        return this.vehicleRepository.save(vehicle);
    }

    public Vehicle fetchVehicleByBienSoXe(String bienSoXe) {
        Optional<Vehicle> vehicleOptional = this.vehicleRepository.findById(bienSoXe);
        if (vehicleOptional.isPresent()) {
            return vehicleOptional.get();
        }
        return null;
    }

    public List<Vehicle> fetchAllVehicles() {
        return this.vehicleRepository.findAll();
    }

    public Vehicle handleUpdateVehicle(Vehicle reqVehicle) {
        Vehicle currentVehicle = this.fetchVehicleByBienSoXe(reqVehicle.getBienSoXe());
        if (currentVehicle != null) {
            currentVehicle.setTenXe(reqVehicle.getTenXe());
            currentVehicle.setMaLoaiXe(reqVehicle.getMaLoaiXe());
            currentVehicle = this.vehicleRepository.save(currentVehicle);
        }
        return currentVehicle;
    }

    public void handleDeleteVehicle(String bienSoXe) {
        this.vehicleRepository.deleteById(bienSoXe);
    }
}
