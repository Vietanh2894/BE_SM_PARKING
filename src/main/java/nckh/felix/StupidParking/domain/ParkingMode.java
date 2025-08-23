package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "parking_modes")
public class ParkingMode {
    @Id
    @Column(name = "ma_hinh_thuc", length = 10, nullable = false)
    @NotBlank(message = "Mã hình thức không được để trống")
    @Size(max = 10, message = "Mã hình thức không được vượt quá 10 ký tự")
    private String maHinhThuc;

    @Column(name = "ten_hinh_thuc", nullable = false, length = 100)
    @NotBlank(message = "Tên hình thức không được để trống")
    @Size(max = 100, message = "Tên hình thức không được vượt quá 100 ký tự")
    private String tenHinhThuc;

    // Default constructor
    public ParkingMode() {
    }

    // Constructor for JSON deserialization from String
    public ParkingMode(String maHinhThuc) {
        this.maHinhThuc = maHinhThuc;
    }

    // Constructor with parameters
    public ParkingMode(String maHinhThuc, String tenHinhThuc) {
        this.maHinhThuc = maHinhThuc;
        this.tenHinhThuc = tenHinhThuc;
    }

    public String getMaHinhThuc() {
        return maHinhThuc;
    }

    public void setMaHinhThuc(String maHinhThuc) {
        this.maHinhThuc = maHinhThuc;
    }

    public String getTenHinhThuc() {
        return tenHinhThuc;
    }

    public void setTenHinhThuc(String tenHinhThuc) {
        this.tenHinhThuc = tenHinhThuc;
    }

    @Override
    public String toString() {
        return "ParkingMode [maHinhThuc=" + maHinhThuc + ", tenHinhThuc=" + tenHinhThuc + "]";
    }
}
