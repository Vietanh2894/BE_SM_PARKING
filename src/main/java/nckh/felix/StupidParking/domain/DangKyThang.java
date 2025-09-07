package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "dang_ky_thang")
public class DangKyThang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bien_so_xe", nullable = false, length = 20)
    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bien_so_xe", referencedColumnName = "BienSoXe", insertable = false, updatable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_nv", nullable = false)
    @NotNull(message = "Nhân viên tạo không được để trống")
    private Staff nhanVienTao;

    @Column(name = "so_thang", nullable = false)
    @Min(value = 1, message = "Số tháng đăng ký phải từ 1 đến 12")
    @Max(value = 12, message = "Số tháng đăng ký phải từ 1 đến 12")
    private Integer soThang;

    @Column(name = "cccd", nullable = false, length = 12)
    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "CCCD phải có đúng 12 số")
    private String cccd;

    @Column(name = "so_cavet", nullable = false, length = 50)
    @NotBlank(message = "Số cavet xe không được để trống")
    @Size(max = 50, message = "Số cavet xe không được vượt quá 50 ký tự")
    private String soCavet;

    @Column(name = "dia_chi", nullable = false, length = 200)
    @NotBlank(message = "Địa chỉ cư trú không được để trống")
    @Size(max = 200, message = "Địa chỉ cư trú không được vượt quá 200 ký tự")
    private String diaChi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_loai_xe", nullable = false)
    @NotNull(message = "Loại xe không được để trống")
    private VehicleType loaiXe;

    @Column(name = "thoi_gian_bat_dau", nullable = false)
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoi_gian_het_han", nullable = false)
    @NotNull(message = "Thời gian hết hạn không được để trống")
    private LocalDateTime thoiGianHetHan;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiDangKy trangThai = TrangThaiDangKy.ACTIVE;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "so_tien_thanh_toan", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Số tiền thanh toán không được để trống")
    @PositiveOrZero(message = "Số tiền thanh toán phải lớn hơn hoặc bằng 0")
    private BigDecimal soTienThanhToan;

    // Enum for registration status
    public enum TrangThaiDangKy {
        ACTIVE("Đang hiệu lực"),
        EXPIRED("Hết hạn"),
        CANCELLED("Đã hủy");

        private final String description;

        TrangThaiDangKy(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Default constructor
    public DangKyThang() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = TrangThaiDangKy.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Business logic methods
    public boolean isActive() {
        return trangThai == TrangThaiDangKy.ACTIVE &&
                thoiGianHetHan != null &&
                LocalDateTime.now().isBefore(thoiGianHetHan);
    }

    public boolean isExpired() {
        return trangThai == TrangThaiDangKy.EXPIRED ||
                (thoiGianHetHan != null && LocalDateTime.now().isAfter(thoiGianHetHan));
    }

    public void expire() {
        this.trangThai = TrangThaiDangKy.EXPIRED;
    }

    public void cancel() {
        this.trangThai = TrangThaiDangKy.CANCELLED;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle != null) {
            this.bienSoXe = vehicle.getBienSoXe();
        }
    }

    public Staff getNhanVienTao() {
        return nhanVienTao;
    }

    public void setNhanVienTao(Staff nhanVienTao) {
        this.nhanVienTao = nhanVienTao;
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

    public VehicleType getLoaiXe() {
        return loaiXe;
    }

    public void setLoaiXe(VehicleType loaiXe) {
        this.loaiXe = loaiXe;
    }

    public LocalDateTime getThoiGianBatDau() {
        return thoiGianBatDau;
    }

    public void setThoiGianBatDau(LocalDateTime thoiGianBatDau) {
        this.thoiGianBatDau = thoiGianBatDau;
    }

    public LocalDateTime getThoiGianHetHan() {
        return thoiGianHetHan;
    }

    public void setThoiGianHetHan(LocalDateTime thoiGianHetHan) {
        this.thoiGianHetHan = thoiGianHetHan;
    }

    public TrangThaiDangKy getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDangKy trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public BigDecimal getSoTienThanhToan() {
        return soTienThanhToan;
    }

    public void setSoTienThanhToan(BigDecimal soTienThanhToan) {
        this.soTienThanhToan = soTienThanhToan;
    }

    @Override
    public String toString() {
        return "DangKyThang [id=" + id + ", bienSoXe=" + bienSoXe + ", soThang=" + soThang +
                ", thoiGianBatDau=" + thoiGianBatDau + ", thoiGianHetHan=" + thoiGianHetHan +
                ", trangThai=" + trangThai + ", soTienThanhToan=" + soTienThanhToan + "]";
    }
}
