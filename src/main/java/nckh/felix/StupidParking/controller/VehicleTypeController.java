package nckh.felix.StupidParking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.service.VehicleTypeService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VehicleTypeController {
    private final VehicleTypeService vehicleTypeService;

    public VehicleTypeController(VehicleTypeService vehicleTypeService) {
        this.vehicleTypeService = vehicleTypeService;
    }

    @PostMapping("/vehicle-types")
    public ResponseEntity<VehicleType> createNewVehicleType(@RequestBody VehicleType xVehicleType) {
        VehicleType vVehicleType = this.vehicleTypeService.handleCreateVehicleType(xVehicleType);
        return ResponseEntity.status(HttpStatus.CREATED).body(vVehicleType);
    }

    @GetMapping("/vehicle-types/{maLoaiXe}")
    public ResponseEntity<VehicleType> getVehicleTypeByMaLoaiXe(@PathVariable("maLoaiXe") String maLoaiXe) {
        VehicleType vVehicleType = this.vehicleTypeService.fetchVehicleTypeByMaLoaiXe(maLoaiXe);
        return ResponseEntity.status(HttpStatus.OK).body(vVehicleType);
    }

    @GetMapping("/vehicle-types")
    public ResponseEntity<List<VehicleType>> getAllVehicleTypes() {
        return ResponseEntity.status(HttpStatus.OK).body(this.vehicleTypeService.fetchAllVehicleTypes());
    }

    @PutMapping("/vehicle-types")
    public ResponseEntity<VehicleType> updateVehicleType(@RequestBody VehicleType xVehicleType) {
        VehicleType vVehicleType = this.vehicleTypeService.handleUpdateVehicleType(xVehicleType);
        return ResponseEntity.ok(vVehicleType);
    }

    @DeleteMapping("/vehicle-types/{maLoaiXe}")
    public ResponseEntity<String> deleteVehicleType(@PathVariable("maLoaiXe") String maLoaiXe) throws IdInvalidException {
        this.vehicleTypeService.handleDeleteVehicleType(maLoaiXe);
        return ResponseEntity.ok("Xóa loại xe thành công");
    }
}
