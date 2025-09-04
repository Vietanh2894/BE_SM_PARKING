package nckh.felix.StupidParking.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ParkingEntryRequestDTO {
    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @NotBlank(message = "Mã bãi đỗ không được để trống")
    private String maBaiDo;

    @NotBlank(message = "Mã loại xe không được để trống")
    private String maLoaiXe;

    private String ghiChu;

    // Constructors
    public ParkingEntryRequestDTO() {
    }

    public ParkingEntryRequestDTO(String bienSoXe, String maBaiDo, String maLoaiXe, String ghiChu) {
        this.bienSoXe = bienSoXe;
        this.maBaiDo = maBaiDo;
        this.maLoaiXe = maLoaiXe;
        this.ghiChu = ghiChu;
    }

    // Getters and Setters
    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public String getMaBaiDo() {
        return maBaiDo;
    }

    public void setMaBaiDo(String maBaiDo) {
        this.maBaiDo = maBaiDo;
    }

    public String getMaLoaiXe() {
        return maLoaiXe;
    }

    public void setMaLoaiXe(String maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
