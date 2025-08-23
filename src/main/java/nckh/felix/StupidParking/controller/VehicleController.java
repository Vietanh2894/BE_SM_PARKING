package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.Vehicle;
import nckh.felix.StupidParking.service.VehicleService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping("/vehicles")
    public ResponseEntity<Vehicle> createNewVehicle(@RequestBody Vehicle xVehicle) {
        Vehicle vVehicle = this.vehicleService.handleCreateVehicle(xVehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(vVehicle);
    }

    @GetMapping("/vehicles/{bienSoXe}")
    public ResponseEntity<Vehicle> getVehicleByBienSoXe(@PathVariable("bienSoXe") String bienSoXe) {
        Vehicle vVehicle = this.vehicleService.fetchVehicleByBienSoXe(bienSoXe);
        return ResponseEntity.status(HttpStatus.OK).body(vVehicle);
    }

    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.status(HttpStatus.OK).body(this.vehicleService.fetchAllVehicles());
    }

    @PutMapping("/vehicles")
    public ResponseEntity<Vehicle> updateVehicle(@RequestBody Vehicle xVehicle) {
        Vehicle vVehicle = this.vehicleService.handleUpdateVehicle(xVehicle);
        return ResponseEntity.ok(vVehicle);
    }

    @DeleteMapping("/vehicles/{bienSoXe}")
    public ResponseEntity<String> deleteVehicle(@PathVariable("bienSoXe") String bienSoXe) throws IdInvalidException {
        this.vehicleService.handleDeleteVehicle(bienSoXe);
        return ResponseEntity.ok("Xóa xe thành công");
    }
}
