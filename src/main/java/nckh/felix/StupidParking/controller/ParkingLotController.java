package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.ParkingLot;
import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.service.ParkingLotService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParkingLotController {
    private final ParkingLotService parkingLotService;

    public ParkingLotController(ParkingLotService parkingLotService) {
        this.parkingLotService = parkingLotService;
    }

    @PostMapping("/parking-lots")
    public ResponseEntity<ParkingLot> createNewParkingLot(@RequestBody ParkingLot xParkingLot) {
        try {
            ParkingLot vParkingLot = this.parkingLotService.handleCreateParkingLot(xParkingLot);
            return ResponseEntity.status(HttpStatus.CREATED).body(vParkingLot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/parking-lots/{maBaiDo}")
    public ResponseEntity<ParkingLot> getParkingLotByMaBaiDo(@PathVariable("maBaiDo") String maBaiDo) {
        ParkingLot vParkingLot = this.parkingLotService.fetchParkingLotByMaBaiDo(maBaiDo);
        return ResponseEntity.status(HttpStatus.OK).body(vParkingLot);
    }

    @GetMapping("/parking-lots")
    public ResponseEntity<List<ParkingLot>> getAllParkingLots() {
        return ResponseEntity.status(HttpStatus.OK).body(this.parkingLotService.fetchAllParkingLots());
    }

    @PutMapping("/parking-lots")
    public ResponseEntity<ParkingLot> updateParkingLot(@RequestBody ParkingLot xParkingLot) {
        try {
            ParkingLot vParkingLot = this.parkingLotService.handleUpdateParkingLot(xParkingLot);
            return ResponseEntity.ok(vParkingLot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/parking-lots/{maBaiDo}")
    public ResponseEntity<String> deleteParkingLot(@PathVariable("maBaiDo") String maBaiDo) throws IdInvalidException {
        this.parkingLotService.handleDeleteParkingLot(maBaiDo);
        return ResponseEntity.ok("Xóa bãi đỗ xe thành công");
    }

    // Business logic endpoints
    @GetMapping("/parking-lots/active")
    public ResponseEntity<List<ParkingLot>> getActiveParkingLots() {
        return ResponseEntity.ok(this.parkingLotService.fetchActiveParkingLots());
    }

    @GetMapping("/parking-lots/available")
    public ResponseEntity<List<ParkingLot>> getAvailableParkingLots() {
        return ResponseEntity.ok(this.parkingLotService.fetchAvailableParkingLots());
    }

    @GetMapping("/parking-lots/vehicle-type/{maLoaiXe}")
    public ResponseEntity<List<ParkingLot>> getParkingLotsByVehicleType(@PathVariable("maLoaiXe") String maLoaiXe) {
        VehicleType vehicleType = new VehicleType(maLoaiXe);
        return ResponseEntity.ok(this.parkingLotService.fetchParkingLotsByVehicleType(vehicleType));
    }

    @GetMapping("/parking-lots/available/vehicle-type/{maLoaiXe}")
    public ResponseEntity<List<ParkingLot>> getAvailableParkingLotsByVehicleType(
            @PathVariable("maLoaiXe") String maLoaiXe) {
        VehicleType vehicleType = new VehicleType(maLoaiXe);
        return ResponseEntity.ok(this.parkingLotService.fetchAvailableParkingLotsByVehicleType(vehicleType));
    }

    @GetMapping("/parking-lots/search")
    public ResponseEntity<List<ParkingLot>> searchParkingLotsByName(@RequestParam("name") String tenBaiDo) {
        return ResponseEntity.ok(this.parkingLotService.searchParkingLotsByName(tenBaiDo));
    }

    @PostMapping("/parking-lots/{maBaiDo}/park")
    public ResponseEntity<ParkingLot> parkVehicle(@PathVariable("maBaiDo") String maBaiDo) {
        try {
            ParkingLot updatedParkingLot = this.parkingLotService.handleParkVehicle(maBaiDo);
            return ResponseEntity.ok(updatedParkingLot);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/parking-lots/{maBaiDo}/unpark")
    public ResponseEntity<ParkingLot> unparkVehicle(@PathVariable("maBaiDo") String maBaiDo) {
        try {
            ParkingLot updatedParkingLot = this.parkingLotService.handleUnparkVehicle(maBaiDo);
            return ResponseEntity.ok(updatedParkingLot);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Statistics endpoints
    @GetMapping("/parking-lots/statistics/capacity")
    public ResponseEntity<List<Object[]>> getTotalCapacityByVehicleType() {
        return ResponseEntity.ok(this.parkingLotService.getTotalCapacityByVehicleType());
    }

    @GetMapping("/parking-lots/statistics/available")
    public ResponseEntity<List<Object[]>> getAvailableSpacesByVehicleType() {
        return ResponseEntity.ok(this.parkingLotService.getAvailableSpacesByVehicleType());
    }
}
