package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.repository.VehicleTypeRepository;

@Service
public class VehicleTypeService {
    private VehicleTypeRepository vehicleTypeRepository;

    public VehicleTypeService(VehicleTypeRepository vehicleTypeRepository) {
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    public VehicleType handleCreateVehicleType(VehicleType vehicleType) {
        return this.vehicleTypeRepository.save(vehicleType);
    }

    public VehicleType fetchVehicleTypeByMaLoaiXe(String maLoaiXe) {
        Optional<VehicleType> vehicleTypeOptional = this.vehicleTypeRepository.findById(maLoaiXe);
        if (vehicleTypeOptional.isPresent()) {
            return vehicleTypeOptional.get();
        }
        return null;
    }

    public List<VehicleType> fetchAllVehicleTypes() {
        return this.vehicleTypeRepository.findAll();
    }

    public VehicleType handleUpdateVehicleType(VehicleType reqVehicleType) {
        VehicleType currentVehicleType = this.fetchVehicleTypeByMaLoaiXe(reqVehicleType.getMaLoaiXe());
        if (currentVehicleType != null) {
            currentVehicleType.setTenLoaiXe(reqVehicleType.getTenLoaiXe());
            currentVehicleType = this.vehicleTypeRepository.save(currentVehicleType);
        }
        return currentVehicleType;
    }

    public void handleDeleteVehicleType(String maLoaiXe) {
        this.vehicleTypeRepository.deleteById(maLoaiXe);
    }
}
