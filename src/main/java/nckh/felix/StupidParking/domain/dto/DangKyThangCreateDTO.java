package nckh.felix.StupidParking.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public class DangKyThangCreateDTO {

    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @NotBlank(message = "Tên xe không được để trống")
    @Size(max = 100, message = "Tên xe không được vượt quá 100 ký tự")
    private String tenXe;

    @NotBlank(message = "Mã nhân viên không được để trống")
    private String maNhanVien;

    @NotNull(message = "Số tháng đăng ký không được để trống")
    @Min(value = 1, message = "Số tháng đăng ký phải từ 1 đến 12")
    @Max(value = 12, message = "Số tháng đăng ký phải từ 1 đến 12")
    private Integer soThang;

    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "CCCD phải có đúng 12 số")
    private String cccd;

    @NotBlank(message = "Số cavet xe không được để trống")
    @Size(max = 50, message = "Số cavet xe không được vượt quá 50 ký tự")
    private String soCavet;

    @NotBlank(message = "Địa chỉ cư trú không được để trống")
    @Size(max = 200, message = "Địa chỉ cư trú không được vượt quá 200 ký tự")
    private String diaChi;

    // Thông tin User để tạo account login (tùy chọn)
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Size(max = 100, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String soDienThoai;

    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String password;

    @NotBlank(message = "Mã loại xe không được để trống")
    private String maLoaiXe;

    // Ngày bắt đầu tùy chọn (nếu null thì dùng ngày hiện tại)
    private String ngayBatDau; // Format: yyyy-MM-dd hoặc yyyy-MM-ddTHH:mm:ss

    private String ghiChu;

    // Face Recognition Integration
    private String faceImageBase64; // Ảnh khuôn mặt dạng base64 (optional)

    private boolean enableFaceRecognition = false; // Có bật nhận diện khuôn mặt không

    // Default constructor
    public DangKyThangCreateDTO() {
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

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public Integer getSoThang() {
        return soThang;
    }

    public void setSoThang(Integer soThang) {
        this.soThang = soThang;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getSoCavet() {
        return soCavet;
    }

    public void setSoCavet(String soCavet) {
        this.soCavet = soCavet;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMaLoaiXe() {
        return maLoaiXe;
    }

    public void setMaLoaiXe(String maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    public String getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(String ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getFaceImageBase64() {
        return faceImageBase64;
    }

    public void setFaceImageBase64(String faceImageBase64) {
        this.faceImageBase64 = faceImageBase64;
    }

    public boolean isEnableFaceRecognition() {
        return enableFaceRecognition;
    }

    public void setEnableFaceRecognition(boolean enableFaceRecognition) {
        this.enableFaceRecognition = enableFaceRecognition;
    }

    @Override
    public String toString() {
        return "DangKyThangCreateDTO [bienSoXe=" + bienSoXe + ", tenXe=" + tenXe +
                ", maNhanVien=" + maNhanVien + ", soThang=" + soThang + ", cccd=" + cccd +
                ", soCavet=" + soCavet + ", diaChi=" + diaChi + ", email=" + email +
                ", soDienThoai=" + soDienThoai + ", maLoaiXe=" + maLoaiXe + "]";
    }
}
