package nckh.felix.StupidParking.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

public class DangKyThangUpdateDTO {

    @NotNull(message = "Số tháng không được để trống")
    @Min(value = 1, message = "Số tháng phải từ 1 đến 12")
    @Max(value = 12, message = "Số tháng phải từ 1 đến 12")
    private Integer soThang;

    @NotNull(message = "Mã nhân viên không được để trống")
    private String maNhanVien;

    // Default constructor
    public DangKyThangUpdateDTO() {
    }

    // Constructor với tham số
    public DangKyThangUpdateDTO(Integer soThang, String maNhanVien) {
        this.soThang = soThang;
        this.maNhanVien = maNhanVien;
    }

    // Getters and setters
    public Integer getSoThang() {
        return soThang;
    }

    public void setSoThang(Integer soThang) {
        this.soThang = soThang;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    @Override
    public String toString() {
        return "DangKyThangUpdateDTO{" +
                "soThang=" + soThang +
                ", maNhanVien='" + maNhanVien + '\'' +
                '}';
    }
}
