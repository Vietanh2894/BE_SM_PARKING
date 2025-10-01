package nckh.felix.StupidParking.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserVehicleCreateDTO {

    @NotBlank(message = "Biển số xe không được để trống")
    @Size(max = 20, message = "Biển số xe không được vượt quá 20 ký tự")
    private String bienSoXe;

    @NotBlank(message = "Tên xe không được để trống")
    @Size(max = 100, message = "Tên xe không được vượt quá 100 ký tự")
    private String tenXe;

    @Size(max = 50, message = "Số cavet xe không được vượt quá 50 ký tự")
    private String soCavet;

    @NotBlank(message = "Mã loại xe không được để trống")
    private String maLoaiXe;

    // Default constructor
    public UserVehicleCreateDTO() {
    }

    // Constructor with parameters
    public UserVehicleCreateDTO(String bienSoXe, String tenXe, String soCavet, String maLoaiXe) {
        this.bienSoXe = bienSoXe;
        this.tenXe = tenXe;
        this.soCavet = soCavet;
        this.maLoaiXe = maLoaiXe;
    }

    // Getters and Setters
    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getTenXe() {
        return tenXe;
    }

    public void setTenXe(String tenXe) {
        this.tenXe = tenXe;
    }

    public String getSoCavet() {
        return soCavet;
    }

    public void setSoCavet(String soCavet) {
        this.soCavet = soCavet;
    }

    public String getMaLoaiXe() {
        return maLoaiXe;
    }

    public void setMaLoaiXe(String maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    @Override
    public String toString() {
        return "UserVehicleCreateDTO [bienSoXe=" + bienSoXe + ", tenXe=" + tenXe +
                ", soCavet=" + soCavet + ", maLoaiXe=" + maLoaiXe + "]";
    }
}