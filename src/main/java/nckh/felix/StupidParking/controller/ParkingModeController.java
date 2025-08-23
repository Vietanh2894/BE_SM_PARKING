package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.ParkingMode;
import nckh.felix.StupidParking.service.ParkingModeService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ParkingModeController {
    private final ParkingModeService parkingModeService;

    public ParkingModeController(ParkingModeService parkingModeService) {
        this.parkingModeService = parkingModeService;
    }

    @PostMapping("/parking-modes")
    public ResponseEntity<ParkingMode> createNewParkingMode(@RequestBody ParkingMode xParkingMode) {
        ParkingMode vParkingMode = this.parkingModeService.handleCreateParkingMode(xParkingMode);
        return ResponseEntity.status(HttpStatus.CREATED).body(vParkingMode);
    }

    @GetMapping("/parking-modes/{maHinhThuc}")
    public ResponseEntity<ParkingMode> getParkingModeByMaHinhThuc(@PathVariable("maHinhThuc") String maHinhThuc) {
        ParkingMode vParkingMode = this.parkingModeService.fetchParkingModeByMaHinhThuc(maHinhThuc);
        return ResponseEntity.status(HttpStatus.OK).body(vParkingMode);
    }

    @GetMapping("/parking-modes")
    public ResponseEntity<List<ParkingMode>> getAllParkingModes() {
        return ResponseEntity.status(HttpStatus.OK).body(this.parkingModeService.fetchAllParkingModes());
    }

    @PutMapping("/parking-modes")
    public ResponseEntity<ParkingMode> updateParkingMode(@RequestBody ParkingMode xParkingMode) {
        ParkingMode vParkingMode = this.parkingModeService.handleUpdateParkingMode(xParkingMode);
        return ResponseEntity.ok(vParkingMode);
    }

    @DeleteMapping("/parking-modes/{maHinhThuc}")
    public ResponseEntity<String> deleteParkingMode(@PathVariable("maHinhThuc") String maHinhThuc)
            throws IdInvalidException {
        this.parkingModeService.handleDeleteParkingMode(maHinhThuc);
        return ResponseEntity.ok("Xóa hình thức đỗ xe thành công");
    }
}
