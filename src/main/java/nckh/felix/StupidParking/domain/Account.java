package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @Column(name = "username", length = 50, nullable = false)
    @NotBlank(message = "Username không được để trống")
    @Size(max = 50, message = "Username không được vượt quá 50 ký tự")
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, max = 255, message = "Password phải có độ dài từ 6 đến 255 ký tự")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai", nullable = false)
    private TrangThaiAccount trangThai = TrangThaiAccount.ENABLE;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    @NotNull(message = "Role không được để trống")
    private Role role;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // Enum for account status
    public enum TrangThaiAccount {
        ENABLE("Kích hoạt"),
        DISABLE("Vô hiệu hóa");

        private final String description;

        TrangThaiAccount(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // Default constructor
    public Account() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    // Constructor for JSON deserialization from String
    public Account(String username) {
        this.username = username;
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = TrangThaiAccount.ENABLE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Business logic methods
    public boolean isActive() {
        return trangThai == TrangThaiAccount.ENABLE;
    }

    public void enableAccount() {
        this.trangThai = TrangThaiAccount.ENABLE;
    }

    public void disableAccount() {
        this.trangThai = TrangThaiAccount.DISABLE;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TrangThaiAccount getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiAccount trangThai) {
        this.trangThai = trangThai;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
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
        return "Account [username=" + username + ", trangThai=" + trangThai + ", role=" + role + "]";
    }
}
