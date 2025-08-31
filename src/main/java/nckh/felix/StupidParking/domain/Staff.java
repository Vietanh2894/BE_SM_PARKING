package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
public class Staff {
    @Id
    @Column(name = "ma_nv", length = 10, nullable = false)
    @NotBlank(message = "Mã nhân viên không được để trống")
    @Size(max = 10, message = "Mã nhân viên không được vượt quá 10 ký tự")
    private String maNV;

    @Column(name = "ho_ten", nullable = false, length = 100)
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 100, message = "Họ tên không được vượt quá 100 ký tự")
    private String hoTen;

    @Column(name = "sdt", nullable = false, length = 15)
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại phải có 10-11 số")
    private String sdt;

    @Column(name = "email", nullable = false, length = 100)
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    @Column(name = "cccd", nullable = false, length = 12)
    @NotBlank(message = "CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{12}$", message = "CCCD phải có đúng 12 số")
    private String cccd;

    @Enumerated(EnumType.STRING)
    @Column(name = "chuc_vu", nullable = false)
    private ChucVu chucVu;

    @Column(name = "ngay_vao_lam", nullable = false)
    @NotNull(message = "Ngày vào làm không được để trống")
    private LocalDate ngayVaoLam;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "username", nullable = false)
    @NotNull(message = "Account không được để trống")
    private Account account;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Enum for staff position
    public enum ChucVu {
        ADMIN("Quản trị viên"),
        BAO_VE("Bảo vệ");

        private final String description;

        ChucVu(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Default constructor
    public Staff() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Constructor for JSON deserialization from String
    public Staff(String maNV) {
        this.maNV = maNV;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Business logic methods
    public boolean isAdmin() {
        return chucVu == ChucVu.ADMIN;
    }

    public boolean isBaoVe() {
        return chucVu == ChucVu.BAO_VE;
    }

    public boolean canManageStaffAndAccount() {
        return isAdmin();
    }

    public boolean isActiveAccount() {
        return account != null && account.isActive();
    }

    // Getters and Setters
    public String getMaNV() {
        return maNV;
    }

    public void setMaNV(String maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    public String getSdt() {
        return sdt;
    }

    public void setSdt(String sdt) {
        this.sdt = sdt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public ChucVu getChucVu() {
        return chucVu;
    }

    public void setChucVu(ChucVu chucVu) {
        this.chucVu = chucVu;
    }

    public LocalDate getNgayVaoLam() {
        return ngayVaoLam;
    }

    public void setNgayVaoLam(LocalDate ngayVaoLam) {
        this.ngayVaoLam = ngayVaoLam;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
        return "Staff [maNV=" + maNV + ", hoTen=" + hoTen + ", chucVu=" + chucVu + ", account=" + account + "]";
    }
}
