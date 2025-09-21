package nckh.felix.StupidParking.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request gia hạn đăng ký tháng
 */
public class DangKyThangExtendDTO {

    @NotNull(message = "Số tháng gia hạn không được để trống")
    @Min(value = 1, message = "Số tháng gia hạn phải từ 1 đến 12")
    @Max(value = 12, message = "Số tháng gia hạn phải từ 1 đến 12")
    private Integer newMonths;

    @NotBlank(message = "Mã nhân viên không được để trống")
    private String maNhanVien;

    // Constructors
    public DangKyThangExtendDTO() {
    }

    public DangKyThangExtendDTO(Integer newMonths, String maNhanVien) {
        this.newMonths = newMonths;
        this.maNhanVien = maNhanVien;
    }

    // Getters and Setters
    public Integer getNewMonths() {
        return newMonths;
    }

    public void setNewMonths(Integer newMonths) {
        this.newMonths = newMonths;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    @Override
    public String toString() {
        return "DangKyThangExtendDTO{" +
                "newMonths=" + newMonths +
                ", maNhanVien='" + maNhanVien + '\'' +
                '}';
    }
}