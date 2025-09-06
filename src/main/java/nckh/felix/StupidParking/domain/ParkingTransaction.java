package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_transactions")
public class ParkingTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long maGiaoDich;

    @Column(name = "bien_so_xe", nullable = false, length = 20)
    @NotBlank(message = "Biển số xe không được để trống")
    private String bienSoXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_bai_do", nullable = false)
    @NotNull(message = "Bãi đỗ không được để trống")
    private ParkingLot parkingLot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_loai_xe", nullable = false)
    @NotNull(message = "Loại xe không được để trống")
    private VehicleType vehicleType;

    @Column(name = "thoi_gian_vao", nullable = false)
    @NotNull(message = "Thời gian vào không được để trống")
    private LocalDateTime thoiGianVao;

    @Column(name = "thoi_gian_ra")
    private LocalDateTime thoiGianRa;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiGiaoDich trangThai = TrangThaiGiaoDich.PENDING_IN;

    @Column(name = "so_tien_thanh_toan", precision = 10, scale = 2)
    @PositiveOrZero(message = "Số tiền thanh toán phải lớn hơn hoặc bằng 0")
    private BigDecimal soTienThanhToan;

    @Column(name = "so_tien", precision = 10, scale = 2)
    private BigDecimal soTien;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nhan_vien_vao")
    private Staff nhanVienVao;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nhan_vien_ra")
    private Staff nhanVienRa;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Enum for transaction status
    public enum TrangThaiGiaoDich {
        PENDING_IN("Chờ duyệt vào"),
        APPROVED_IN("Đã duyệt vào"),
        PENDING_OUT("Chờ duyệt ra"),
        COMPLETED("Hoàn thành"),
        CANCELLED("Hủy bỏ");

        private final String description;

        TrangThaiGiaoDich(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Default constructor
    public ParkingTransaction() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.soTien = BigDecimal.ZERO; // Khởi tạo giá trị mặc định cho so_tien
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = TrangThaiGiaoDich.PENDING_IN;
        }
        if (thoiGianVao == null) {
            thoiGianVao = LocalDateTime.now();
        }
        if (soTien == null) {
            soTien = BigDecimal.ZERO;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Business logic methods
    public boolean isActive() {
        return trangThai == TrangThaiGiaoDich.APPROVED_IN;
    }

    public boolean canCheckOut() {
        return trangThai == TrangThaiGiaoDich.APPROVED_IN;
    }

    public void approveEntry(Staff staff) {
        this.nhanVienVao = staff;
        this.trangThai = TrangThaiGiaoDich.APPROVED_IN;
    }

    public void requestExit() {
        if (!canCheckOut()) {
            throw new IllegalStateException("Xe chưa được duyệt vào hoặc đã ra");
        }
        this.thoiGianRa = LocalDateTime.now();
        this.trangThai = TrangThaiGiaoDich.PENDING_OUT;
    }

    public void completeTransaction(Staff staff, BigDecimal amount) {
        this.nhanVienRa = staff;
        this.soTienThanhToan = amount;
        this.trangThai = TrangThaiGiaoDich.COMPLETED;
        if (this.thoiGianRa == null) {
            this.thoiGianRa = LocalDateTime.now();
        }
    }

    public void cancel() {
        this.trangThai = TrangThaiGiaoDich.CANCELLED;
    }

    // Calculate parking duration in hours (round up only if over 30 minutes)
    public long getParkingDurationInHours() {
        if (thoiGianVao == null)
            return 0;
        LocalDateTime endTime = thoiGianRa != null ? thoiGianRa : LocalDateTime.now();

        // Tính tổng số phút gửi xe
        long totalMinutes = java.time.Duration.between(thoiGianVao, endTime).toMinutes();

        // Tính số giờ đầy đủ
        long fullHours = totalMinutes / 60;

        // Tính số phút lẻ
        long remainingMinutes = totalMinutes % 60;

        // Chỉ cộng thêm 1 giờ nếu phút lẻ > 30
        if (remainingMinutes > 30) {
            fullHours++;
        }

        // Tối thiểu 1 giờ
        return Math.max(fullHours, 1);
    } // Getters and Setters

    public Long getMaGiaoDich() {
        return maGiaoDich;
    }

    public void setMaGiaoDich(Long maGiaoDich) {
        this.maGiaoDich = maGiaoDich;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(ParkingLot parkingLot) {
        this.parkingLot = parkingLot;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDateTime getThoiGianVao() {
        return thoiGianVao;
    }

    public void setThoiGianVao(LocalDateTime thoiGianVao) {
        this.thoiGianVao = thoiGianVao;
    }

    public LocalDateTime getThoiGianRa() {
        return thoiGianRa;
    }

    public void setThoiGianRa(LocalDateTime thoiGianRa) {
        this.thoiGianRa = thoiGianRa;
    }

    public TrangThaiGiaoDich getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiGiaoDich trangThai) {
        this.trangThai = trangThai;
    }

    public BigDecimal getSoTienThanhToan() {
        return soTienThanhToan;
    }

    public void setSoTienThanhToan(BigDecimal soTienThanhToan) {
        this.soTienThanhToan = soTienThanhToan;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public Staff getNhanVienVao() {
        return nhanVienVao;
    }

    public void setNhanVienVao(Staff nhanVienVao) {
        this.nhanVienVao = nhanVienVao;
    }

    public Staff getNhanVienRa() {
        return nhanVienRa;
    }

    public void setNhanVienRa(Staff nhanVienRa) {
        this.nhanVienRa = nhanVienRa;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
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

    @Override
    public String toString() {
        return "ParkingTransaction [maGiaoDich=" + maGiaoDich + ", bienSoXe=" + bienSoXe +
                ", trangThai=" + trangThai + ", thoiGianVao=" + thoiGianVao +
                ", thoiGianRa=" + thoiGianRa + "]";
    }
}
