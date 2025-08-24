package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_lots")
public class ParkingLot {
    @Id
    @Column(name = "ma_bai_do", length = 10, nullable = false)
    @NotBlank(message = "Mã bãi đỗ không được để trống")
    @Size(max = 10, message = "Mã bãi đỗ không được vượt quá 10 ký tự")
    private String maBaiDo;

    @Column(name = "ten_bai_do", nullable = false, length = 100)
    @NotBlank(message = "Tên bãi đỗ không được để trống")
    @Size(max = 100, message = "Tên bãi đỗ không được vượt quá 100 ký tự")
    private String tenBaiDo;

    @Column(name = "so_cho_trong", nullable = false)
    @NotNull(message = "Số chỗ trống không được để trống")
    @PositiveOrZero(message = "Số chỗ trống phải lớn hơn hoặc bằng 0")
    private Integer soChoTrong;

    @Column(name = "tong_so_cho", nullable = false)
    @NotNull(message = "Tổng số chỗ không được để trống")
    @PositiveOrZero(message = "Tổng số chỗ phải lớn hơn hoặc bằng 0")
    private Integer tongSoCho;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_loai_xe", nullable = false)
    @NotNull(message = "Loại xe không được để trống")
    private VehicleType maLoaiXe;

    @Column(name = "dia_chi", length = 255)
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String diaChi;

    @Column(name = "mo_ta", length = 500)
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String moTa;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiBaiDo trangThai = TrangThaiBaiDo.ACTIVE;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Enum for parking lot status
    public enum TrangThaiBaiDo {
        ACTIVE("Hoạt động"),
        INACTIVE("Không hoạt động"),
        MAINTENANCE("Bảo trì");

        private final String description;

        TrangThaiBaiDo(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Default constructor
    public ParkingLot() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Constructor for JSON deserialization from String
    public ParkingLot(String maBaiDo) {
        this.maBaiDo = maBaiDo;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = TrangThaiBaiDo.ACTIVE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Business logic methods
    public boolean hasAvailableSpace() {
        return soChoTrong > 0;
    }

    public boolean canPark() {
        return hasAvailableSpace() && trangThai == TrangThaiBaiDo.ACTIVE;
    }

    public void parkVehicle() {
        if (canPark()) {
            soChoTrong--;
        }
    }

    public void unparkVehicle() {
        if (soChoTrong < tongSoCho) {
            soChoTrong++;
        }
    }

    public double getOccupancyRate() {
        if (tongSoCho == 0)
            return 0;
        return ((double) (tongSoCho - soChoTrong) / tongSoCho) * 100;
    }

    // Getters and Setters
    public String getMaBaiDo() {
        return maBaiDo;
    }

    public void setMaBaiDo(String maBaiDo) {
        this.maBaiDo = maBaiDo;
    }

    public String getTenBaiDo() {
        return tenBaiDo;
    }

    public void setTenBaiDo(String tenBaiDo) {
        this.tenBaiDo = tenBaiDo;
    }

    public Integer getSoChoTrong() {
        return soChoTrong;
    }

    public void setSoChoTrong(Integer soChoTrong) {
        this.soChoTrong = soChoTrong;
    }

    public Integer getTongSoCho() {
        return tongSoCho;
    }

    public void setTongSoCho(Integer tongSoCho) {
        this.tongSoCho = tongSoCho;
    }

    public VehicleType getMaLoaiXe() {
        return maLoaiXe;
    }

    public void setMaLoaiXe(VehicleType maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public TrangThaiBaiDo getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiBaiDo trangThai) {
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

    @Override
    public String toString() {
        return "ParkingLot [maBaiDo=" + maBaiDo + ", tenBaiDo=" + tenBaiDo + ", soChoTrong=" + soChoTrong
                + ", tongSoCho=" + tongSoCho + ", maLoaiXe=" + maLoaiXe + ", trangThai=" + trangThai + "]";
    }
}
