package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.ParkingMode;
import nckh.felix.StupidParking.repository.ParkingModeRepository;

@Service
public class ParkingModeService {
    private ParkingModeRepository parkingModeRepository;

    public ParkingModeService(ParkingModeRepository parkingModeRepository) {
        this.parkingModeRepository = parkingModeRepository;
    }

    public ParkingMode handleCreateParkingMode(ParkingMode parkingMode) {
        return this.parkingModeRepository.save(parkingMode);
    }

    public ParkingMode fetchParkingModeByMaHinhThuc(String maHinhThuc) {
        Optional<ParkingMode> parkingModeOptional = this.parkingModeRepository.findById(maHinhThuc);
        if (parkingModeOptional.isPresent()) {
            return parkingModeOptional.get();
        }
        return null;
    }

    public List<ParkingMode> fetchAllParkingModes() {
        return this.parkingModeRepository.findAll();
    }

    public ParkingMode handleUpdateParkingMode(ParkingMode reqParkingMode) {
        ParkingMode currentParkingMode = this.fetchParkingModeByMaHinhThuc(reqParkingMode.getMaHinhThuc());
        if (currentParkingMode != null) {
            currentParkingMode.setTenHinhThuc(reqParkingMode.getTenHinhThuc());
            currentParkingMode = this.parkingModeRepository.save(currentParkingMode);
        }
        return currentParkingMode;
    }

    public void handleDeleteParkingMode(String maHinhThuc) {
        this.parkingModeRepository.deleteById(maHinhThuc);
    }
}
