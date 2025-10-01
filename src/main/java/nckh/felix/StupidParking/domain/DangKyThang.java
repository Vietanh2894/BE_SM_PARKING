package nckh.felix.StupidParking.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class DangKyThang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bien_so_xe", nullable = false, length = 20)
    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @ManyToOne(fetch = FetchType.LAZY)
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
    @Column(name = "trang_thai", nullable = false, length = 20)
    private TrangThaiDangKy trangThai = TrangThaiDangKy.PENDING;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai_thanh_toan", nullable = false, length = 20)
    private TrangThaiThanhToan trangThaiThanhToan = TrangThaiThanhToan.PENDING;

    // Trường để quản lý lịch sử gia hạn
    @Column(name = "parent_id")
    private Long parentId; // NULL = đăng ký gốc, có giá trị = gia hạn từ đăng ký khác

    @Column(name = "lan_gia_han", nullable = false)
    private Integer lanGiaHan = 0; // 0 = đăng ký gốc, 1,2,3... = số lần gia hạn

    // Enum for payment status
    public enum TrangThaiThanhToan {
        PENDING("Chờ thanh toán"),
        PAID("Đã thanh toán");

        private final String description;

        TrangThaiThanhToan(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Enum for registration status
    public enum TrangThaiDangKy {
        PENDING("Chờ xử lý"),
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
            trangThai = TrangThaiDangKy.PENDING;
        }
        if (trangThaiThanhToan == null) {
            trangThaiThanhToan = TrangThaiThanhToan.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Business logic methods
    public boolean isActive() {
        return trangThai == TrangThaiDangKy.ACTIVE &&
                trangThaiThanhToan == TrangThaiThanhToan.PAID &&
                thoiGianHetHan != null &&
                LocalDateTime.now().isBefore(thoiGianHetHan);
    }

    public boolean isExpired() {
        return trangThai == TrangThaiDangKy.EXPIRED ||
                (thoiGianHetHan != null && LocalDateTime.now().isAfter(thoiGianHetHan));
    }

    public boolean isPending() {
        return trangThai == TrangThaiDangKy.PENDING || trangThaiThanhToan == TrangThaiThanhToan.PENDING;
    }

    public boolean canEdit() {
        return trangThaiThanhToan == TrangThaiThanhToan.PENDING;
    }

    public void expire() {
        this.trangThai = TrangThaiDangKy.EXPIRED;
    }

    public void cancel() {
        this.trangThai = TrangThaiDangKy.CANCELLED;
    }

    public void completePayment() {
        this.trangThaiThanhToan = TrangThaiThanhToan.PAID;
        this.trangThai = TrangThaiDangKy.ACTIVE;
    }

    public void extendMonths(int additionalMonths, BigDecimal pricePerMonth) {
        if (!canEdit() && !isExpired()) {
            throw new IllegalStateException("Không thể gia hạn khi đăng ký đã hoàn tất thanh toán và chưa hết hạn");
        }

        this.soThang += additionalMonths;
        this.thoiGianHetHan = this.thoiGianHetHan.plusMonths(additionalMonths);
        this.soTienThanhToan = this.soTienThanhToan.add(pricePerMonth.multiply(BigDecimal.valueOf(additionalMonths)));

        if (isExpired()) {
            this.trangThai = TrangThaiDangKy.ACTIVE;
        }
    }

    public void updateMonths(int newMonthCount, BigDecimal pricePerMonth) {
        if (!canEdit()) {
            throw new IllegalStateException("Không thể cập nhật số tháng khi đã hoàn tất thanh toán");
        }

        this.soThang = newMonthCount;
        this.thoiGianHetHan = this.thoiGianBatDau.plusMonths(newMonthCount);
        this.soTienThanhToan = pricePerMonth.multiply(BigDecimal.valueOf(newMonthCount));
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

    public TrangThaiThanhToan getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(TrangThaiThanhToan trangThaiThanhToan) {
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLanGiaHan() {
        return lanGiaHan;
    }

    public void setLanGiaHan(Integer lanGiaHan) {
        this.lanGiaHan = lanGiaHan;
    }

    // Business methods để quản lý gia hạn
    public boolean isRoot() {
        return parentId == null;
    }

    public boolean isExtension() {
        return parentId != null;
    }

    @Override
    public String toString() {
        return "DangKyThang [id=" + id + ", bienSoXe=" + bienSoXe + ", soThang=" + soThang +
                ", thoiGianBatDau=" + thoiGianBatDau + ", thoiGianHetHan=" + thoiGianHetHan +
                ", trangThai=" + trangThai + ", soTienThanhToan=" + soTienThanhToan + "]";
    }
}
