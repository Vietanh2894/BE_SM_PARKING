package nckh.felix.StupidParking.domain.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

public class ParkingExitRequestDTO {
    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @PositiveOrZero(message = "Số tiền thanh toán phải lớn hơn hoặc bằng 0")
    private BigDecimal soTienThanhToan;

    private String ghiChu;

    // Constructors
    public ParkingExitRequestDTO() {
    }

    public ParkingExitRequestDTO(String bienSoXe, BigDecimal soTienThanhToan, String ghiChu) {
        this.bienSoXe = bienSoXe;
        this.soTienThanhToan = soTienThanhToan;
        this.ghiChu = ghiChu;
    }

    // Getters and Setters
    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public BigDecimal getSoTienThanhToan() {
        return soTienThanhToan;
    }

    public void setSoTienThanhToan(BigDecimal soTienThanhToan) {
        this.soTienThanhToan = soTienThanhToan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
