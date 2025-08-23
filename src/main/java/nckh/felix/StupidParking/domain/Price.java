package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "prices")
public class Price {
    @Id
    @Column(name = "ma_bang_gia", length = 10, nullable = false)
    @NotBlank(message = "Mã bảng giá không được để trống")
    @Size(max = 10, message = "Mã bảng giá không được vượt quá 10 ký tự")
    private String maBangGia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_loai_xe", nullable = false)
    @NotNull(message = "Loại xe không được để trống")
    private VehicleType maLoaiXe;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ma_hinh_thuc", nullable = false)
    @NotNull(message = "Hình thức đỗ xe không được để trống")
    private ParkingMode maHinhThuc;

    @Column(name = "gia", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Giá không được để trống")
    @PositiveOrZero(message = "Giá phải lớn hơn hoặc bằng 0")
    private BigDecimal gia;

    // Default constructor
    public Price() {
    }

    // Constructor with parameters
    public Price(String maBangGia, VehicleType maLoaiXe, ParkingMode maHinhThuc, BigDecimal gia) {
        this.maBangGia = maBangGia;
        this.maLoaiXe = maLoaiXe;
        this.maHinhThuc = maHinhThuc;
        this.gia = gia;
    }

    public String getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(String maBangGia) {
        this.maBangGia = maBangGia;
    }

    public VehicleType getMaLoaiXe() {
        return maLoaiXe;
    }

    public void setMaLoaiXe(VehicleType maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    public ParkingMode getMaHinhThuc() {
        return maHinhThuc;
    }

    public void setMaHinhThuc(ParkingMode maHinhThuc) {
        this.maHinhThuc = maHinhThuc;
    }

    public BigDecimal getGia() {
        return gia;
    }

    public void setGia(BigDecimal gia) {
        this.gia = gia;
    }

    @Override
    public String toString() {
        return "Price [maBangGia=" + maBangGia + ", maLoaiXe=" + maLoaiXe + ", maHinhThuc=" + maHinhThuc + ", gia="
                + gia + "]";
    }
}
