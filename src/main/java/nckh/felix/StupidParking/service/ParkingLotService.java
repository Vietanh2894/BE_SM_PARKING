package nckh.felix.StupidParking.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.ParkingLot;
import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.domain.ParkingLot.TrangThaiBaiDo;
import nckh.felix.StupidParking.repository.ParkingLotRepository;

@Service
public class ParkingLotService {
    private ParkingLotRepository parkingLotRepository;

    public ParkingLotService(ParkingLotRepository parkingLotRepository) {
        this.parkingLotRepository = parkingLotRepository;
    }

    public ParkingLot handleCreateParkingLot(ParkingLot parkingLot) {
        // Validation: số chỗ trống không được lớn hơn tổng số chỗ
        if (parkingLot.getSoChoTrong() > parkingLot.getTongSoCho()) {
            throw new IllegalArgumentException("Số chỗ trống không được lớn hơn tổng số chỗ");
        }
        return this.parkingLotRepository.save(parkingLot);
    }

    public ParkingLot fetchParkingLotByMaBaiDo(String maBaiDo) {
        Optional<ParkingLot> parkingLotOptional = this.parkingLotRepository.findById(maBaiDo);
        if (parkingLotOptional.isPresent()) {
            return parkingLotOptional.get();
        }
        return null;
    }

    public List<ParkingLot> fetchAllParkingLots() {
        return this.parkingLotRepository.findAll();
    }

    public ParkingLot handleUpdateParkingLot(ParkingLot reqParkingLot) {
        ParkingLot currentParkingLot = this.fetchParkingLotByMaBaiDo(reqParkingLot.getMaBaiDo());
        if (currentParkingLot != null) {
            // Validation: số chỗ trống không được lớn hơn tổng số chỗ
            if (reqParkingLot.getSoChoTrong() > reqParkingLot.getTongSoCho()) {
                throw new IllegalArgumentException("Số chỗ trống không được lớn hơn tổng số chỗ");
            }

            currentParkingLot.setTenBaiDo(reqParkingLot.getTenBaiDo());
            currentParkingLot.setSoChoTrong(reqParkingLot.getSoChoTrong());
            currentParkingLot.setTongSoCho(reqParkingLot.getTongSoCho());
            currentParkingLot.setMaLoaiXe(reqParkingLot.getMaLoaiXe());
            currentParkingLot.setDiaChi(reqParkingLot.getDiaChi());
            currentParkingLot.setMoTa(reqParkingLot.getMoTa());
            currentParkingLot.setTrangThai(reqParkingLot.getTrangThai());

            currentParkingLot = this.parkingLotRepository.save(currentParkingLot);
        }
        return currentParkingLot;
    }

    public void handleDeleteParkingLot(String maBaiDo) {
        this.parkingLotRepository.deleteById(maBaiDo);
    }

    // Business logic methods
    public List<ParkingLot> fetchParkingLotsByVehicleType(VehicleType vehicleType) {
        return this.parkingLotRepository.findByMaLoaiXe(vehicleType);
    }

    public List<ParkingLot> fetchActiveParkingLots() {
        return this.parkingLotRepository.findByTrangThai(TrangThaiBaiDo.ACTIVE);
    }

    public List<ParkingLot> fetchAvailableParkingLots() {
        return this.parkingLotRepository.findAvailableParkingLots();
    }

    public List<ParkingLot> fetchAvailableParkingLotsByVehicleType(VehicleType vehicleType) {
        return this.parkingLotRepository.findAvailableParkingLotsByVehicleType(vehicleType);
    }

    public List<ParkingLot> searchParkingLotsByName(String tenBaiDo) {
        return this.parkingLotRepository.findByTenBaiDoContaining(tenBaiDo);
    }

    public ParkingLot handleParkVehicle(String maBaiDo) {
        ParkingLot parkingLot = this.fetchParkingLotByMaBaiDo(maBaiDo);
        if (parkingLot == null) {
            throw new IllegalArgumentException("Không tìm thấy bãi đỗ với mã: " + maBaiDo);
        }

        if (!parkingLot.canPark()) {
            throw new IllegalStateException("Bãi đỗ không thể đỗ thêm xe (hết chỗ hoặc không hoạt động)");
        }

        parkingLot.parkVehicle();
        return this.parkingLotRepository.save(parkingLot);
    }

    public ParkingLot handleUnparkVehicle(String maBaiDo) {
        ParkingLot parkingLot = this.fetchParkingLotByMaBaiDo(maBaiDo);
        if (parkingLot == null) {
            throw new IllegalArgumentException("Không tìm thấy bãi đỗ với mã: " + maBaiDo);
        }

        parkingLot.unparkVehicle();
        return this.parkingLotRepository.save(parkingLot);
    }

    // Statistics methods
    public List<Object[]> getTotalCapacityByVehicleType() {
        return this.parkingLotRepository.getTotalCapacityByVehicleType();
    }

    public List<Object[]> getAvailableSpacesByVehicleType() {
        return this.parkingLotRepository.getAvailableSpacesByVehicleType();
    }
}
