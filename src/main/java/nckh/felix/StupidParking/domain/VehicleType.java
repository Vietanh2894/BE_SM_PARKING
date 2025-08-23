package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "vehicle_types")
public class VehicleType {
    @Id
    @Column(name = "ma_loai_xe", length = 10, nullable = false)
    @NotBlank(message = "Loại xe không được để trống")
    @Size(max = 10, message = "Mã loại xe không được vượt quá 10 ký tự")
    private String maLoaiXe;

    @Column(name = "ten_loai_xe", nullable = false, length = 50)
    @NotBlank(message = "Tên loại xe không được để trống")
    @Size(max = 50, message = "Tên loại xe không được vượt quá 50 ký tự")
    private String tenLoaiXe;

    // Bidirectional relationship - một loại xe có thể có nhiều xe
    @OneToMany(mappedBy = "maLoaiXe", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Vehicle> vehicles;

    // Default constructor
    public VehicleType() {
    }

    // Constructor for JSON deserialization from String
    public VehicleType(String maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    public String getMaLoaiXe() {
        return maLoaiXe;
    }

    public void setMaLoaiXe(String maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    public String getTenLoaiXe() {
        return tenLoaiXe;
    }

    public void setTenLoaiXe(String tenLoaiXe) {
        this.tenLoaiXe = tenLoaiXe;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    // Helper methods for bidirectional relationship
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(vehicle);
        vehicle.setMaLoaiXe(this);
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
        vehicle.setMaLoaiXe(null);
    }

    @Override
    public String toString() {
        return "VehicleType [maLoaiXe=" + maLoaiXe + ", tenLoaiXe=" + tenLoaiXe + "]";
    }

}
