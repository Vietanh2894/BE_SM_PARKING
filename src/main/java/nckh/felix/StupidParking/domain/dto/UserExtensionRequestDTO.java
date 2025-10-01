package nckh.felix.StupidParking.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO cho yêu cầu gia hạn đăng ký tháng từ User
 */
public class UserExtensionRequestDTO {
    @NotNull(message = "ID đăng ký tháng không được để trống")
    private Long dangKyThangId; // ID của đăng ký tháng hiện tại cần gia hạn

    @NotNull(message = "Số tháng gia hạn không được để trống")
    @Min(value = 1, message = "Số tháng gia hạn phải từ 1 đến 12")
    @Max(value = 12, message = "Số tháng gia hạn phải từ 1 đến 12")
    private Integer soThangGiaHan;

    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String ghiChu;

    // Constructors
    public UserExtensionRequestDTO() {
    }

    public UserExtensionRequestDTO(Long dangKyThangId, Integer soThangGiaHan, String ghiChu) {
        this.dangKyThangId = dangKyThangId;
        this.soThangGiaHan = soThangGiaHan;
        this.ghiChu = ghiChu;
    }

    // Getters and Setters
    public Long getDangKyThangId() {
        return dangKyThangId;
    }

    public void setDangKyThangId(Long dangKyThangId) {
        this.dangKyThangId = dangKyThangId;
    }

    public Integer getSoThangGiaHan() {
        return soThangGiaHan;
    }

    public void setSoThangGiaHan(Integer soThangGiaHan) {
        this.soThangGiaHan = soThangGiaHan;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    @Override
    public String toString() {
        return "UserExtensionRequestDTO{" +
                "dangKyThangId=" + dangKyThangId +
                ", soThangGiaHan=" + soThangGiaHan +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}