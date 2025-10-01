package nckh.felix.StupidParking.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public class UserMonthlyRegistrationRequestDTO {

    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @NotNull(message = "Số tháng đăng ký không được để trống")
    @Min(value = 1, message = "Số tháng đăng ký phải từ 1 đến 12")
    @Max(value = 12, message = "Số tháng đăng ký phải từ 1 đến 12")
    private Integer soThang;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String ghiChu;

    // Ngày bắt đầu mong muốn (tùy chọn)
    private String ngayBatDauMongMuon; // Format: yyyy-MM-dd

    // Default constructor
    public UserMonthlyRegistrationRequestDTO() {
    }

    // Constructor with parameters
    public UserMonthlyRegistrationRequestDTO(String bienSoXe, Integer soThang, String ghiChu,
            String ngayBatDauMongMuon) {
        this.bienSoXe = bienSoXe;
        this.soThang = soThang;
        this.ghiChu = ghiChu;
        this.ngayBatDauMongMuon = ngayBatDauMongMuon;
    }

    // Getters and Setters
    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public Integer getSoThang() {
        return soThang;
    }

    public void setSoThang(Integer soThang) {
        this.soThang = soThang;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getNgayBatDauMongMuon() {
        return ngayBatDauMongMuon;
    }

    public void setNgayBatDauMongMuon(String ngayBatDauMongMuon) {
        this.ngayBatDauMongMuon = ngayBatDauMongMuon;
    }

    @Override
    public String toString() {
        return "UserMonthlyRegistrationRequestDTO [bienSoXe=" + bienSoXe + ", soThang=" + soThang +
                ", ghiChu=" + ghiChu + ", ngayBatDauMongMuon=" + ngayBatDauMongMuon + "]";
    }
}