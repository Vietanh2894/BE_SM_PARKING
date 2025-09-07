package nckh.felix.StupidParking.domain.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

public class UserDashboardDTO {
    private UserInfo userInfo;
    private List<VehicleInfo> vehicles;
    private List<DangKyThangInfo> dangKyThangs;
    private DashboardSummary summary;

    // Constructors
    public UserDashboardDTO() {
    }

    public UserDashboardDTO(UserInfo userInfo, List<VehicleInfo> vehicles,
            List<DangKyThangInfo> dangKyThangs, DashboardSummary summary) {
        this.userInfo = userInfo;
        this.vehicles = vehicles;
        this.dangKyThangs = dangKyThangs;
        this.summary = summary;
    }

    // Getters and Setters
    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public List<VehicleInfo> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<VehicleInfo> vehicles) {
        this.vehicles = vehicles;
    }

    public List<DangKyThangInfo> getDangKyThangs() {
        return dangKyThangs;
    }

    public void setDangKyThangs(List<DangKyThangInfo> dangKyThangs) {
        this.dangKyThangs = dangKyThangs;
    }

    public DashboardSummary getSummary() {
        return summary;
    }

    public void setSummary(DashboardSummary summary) {
        this.summary = summary;
    }

    // Inner Classes
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String cccd;
        private String sdt;
        private String diaChi;
        private LocalDateTime createdDate;

        // Constructors
        public UserInfo() {
        }

        public UserInfo(Long id, String name, String email, String cccd, String sdt, String diaChi,
                LocalDateTime createdDate) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.cccd = cccd;
            this.sdt = sdt;
            this.diaChi = diaChi;
            this.createdDate = createdDate;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
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

        public String getSdt() {
            return sdt;
        }

        public void setSdt(String sdt) {
            this.sdt = sdt;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public void setDiaChi(String diaChi) {
            this.diaChi = diaChi;
        }

        public LocalDateTime getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
        }
    }

    public static class VehicleInfo {
        private String bienSoXe;
        private String tenXe;
        private String maLoaiXe;
        private String tenLoaiXe;
        private LocalDateTime createdDate;
        private boolean hasActiveDangKy;
        private LocalDateTime dangKyExpiry;

        // Constructors
        public VehicleInfo() {
        }

        public VehicleInfo(String bienSoXe, String tenXe, String maLoaiXe, String tenLoaiXe,
                LocalDateTime createdDate, boolean hasActiveDangKy, LocalDateTime dangKyExpiry) {
            this.bienSoXe = bienSoXe;
            this.tenXe = tenXe;
            this.maLoaiXe = maLoaiXe;
            this.tenLoaiXe = tenLoaiXe;
            this.createdDate = createdDate;
            this.hasActiveDangKy = hasActiveDangKy;
            this.dangKyExpiry = dangKyExpiry;
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

        public String getMaLoaiXe() {
            return maLoaiXe;
        }

        public void setMaLoaiXe(String maLoaiXe) {
            this.maLoaiXe = maLoaiXe;
        }

        public String getTenLoaiXe() {
            return tenLoaiXe;
        }

        public void setTenLoaiXe(String tenLoaiXe) {
            this.tenLoaiXe = tenLoaiXe;
        }

        public LocalDateTime getCreatedDate() {
            return createdDate;
        }

        public void setCreatedDate(LocalDateTime createdDate) {
            this.createdDate = createdDate;
        }

        public boolean isHasActiveDangKy() {
            return hasActiveDangKy;
        }

        public void setHasActiveDangKy(boolean hasActiveDangKy) {
            this.hasActiveDangKy = hasActiveDangKy;
        }

        public LocalDateTime getDangKyExpiry() {
            return dangKyExpiry;
        }

        public void setDangKyExpiry(LocalDateTime dangKyExpiry) {
            this.dangKyExpiry = dangKyExpiry;
        }
    }

    public static class DangKyThangInfo {
        private Long id;
        private String bienSoXe;
        private String tenXe;
        private Integer soThang;
        private BigDecimal soTienThanhToan;
        private LocalDateTime thoiGianBatDau;
        private LocalDateTime thoiGianHetHan;
        private String trangThai;
        private String ghiChu;
        private boolean isActive;
        private boolean isExpired;
        private long daysUntilExpiry;

        // Constructors
        public DangKyThangInfo() {
        }

        public DangKyThangInfo(Long id, String bienSoXe, String tenXe, Integer soThang, BigDecimal soTienThanhToan,
                LocalDateTime thoiGianBatDau, LocalDateTime thoiGianHetHan, String trangThai, String ghiChu,
                boolean isActive, boolean isExpired, long daysUntilExpiry) {
            this.id = id;
            this.bienSoXe = bienSoXe;
            this.tenXe = tenXe;
            this.soThang = soThang;
            this.soTienThanhToan = soTienThanhToan;
            this.thoiGianBatDau = thoiGianBatDau;
            this.thoiGianHetHan = thoiGianHetHan;
            this.trangThai = trangThai;
            this.ghiChu = ghiChu;
            this.isActive = isActive;
            this.isExpired = isExpired;
            this.daysUntilExpiry = daysUntilExpiry;
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

        public String getTenXe() {
            return tenXe;
        }

        public void setTenXe(String tenXe) {
            this.tenXe = tenXe;
        }

        public Integer getSoThang() {
            return soThang;
        }

        public void setSoThang(Integer soThang) {
            this.soThang = soThang;
        }

        public BigDecimal getSoTienThanhToan() {
            return soTienThanhToan;
        }

        public void setSoTienThanhToan(BigDecimal soTienThanhToan) {
            this.soTienThanhToan = soTienThanhToan;
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

        public String getTrangThai() {
            return trangThai;
        }

        public void setTrangThai(String trangThai) {
            this.trangThai = trangThai;
        }

        public String getGhiChu() {
            return ghiChu;
        }

        public void setGhiChu(String ghiChu) {
            this.ghiChu = ghiChu;
        }

        public boolean isActive() {
            return isActive;
        }

        public void setActive(boolean active) {
            isActive = active;
        }

        public boolean isExpired() {
            return isExpired;
        }

        public void setExpired(boolean expired) {
            isExpired = expired;
        }

        public long getDaysUntilExpiry() {
            return daysUntilExpiry;
        }

        public void setDaysUntilExpiry(long daysUntilExpiry) {
            this.daysUntilExpiry = daysUntilExpiry;
        }
    }

    public static class DashboardSummary {
        private int totalVehicles;
        private int activeRegistrations;
        private int expiredRegistrations;
        private BigDecimal totalAmountPaid;
        private LocalDateTime nextExpiryDate;
        private String nextExpiryVehicle;

        // Constructors
        public DashboardSummary() {
        }

        public DashboardSummary(int totalVehicles, int activeRegistrations, int expiredRegistrations,
                BigDecimal totalAmountPaid, LocalDateTime nextExpiryDate, String nextExpiryVehicle) {
            this.totalVehicles = totalVehicles;
            this.activeRegistrations = activeRegistrations;
            this.expiredRegistrations = expiredRegistrations;
            this.totalAmountPaid = totalAmountPaid;
            this.nextExpiryDate = nextExpiryDate;
            this.nextExpiryVehicle = nextExpiryVehicle;
        }

        // Getters and Setters
        public int getTotalVehicles() {
            return totalVehicles;
        }

        public void setTotalVehicles(int totalVehicles) {
            this.totalVehicles = totalVehicles;
        }

        public int getActiveRegistrations() {
            return activeRegistrations;
        }

        public void setActiveRegistrations(int activeRegistrations) {
            this.activeRegistrations = activeRegistrations;
        }

        public int getExpiredRegistrations() {
            return expiredRegistrations;
        }

        public void setExpiredRegistrations(int expiredRegistrations) {
            this.expiredRegistrations = expiredRegistrations;
        }

        public BigDecimal getTotalAmountPaid() {
            return totalAmountPaid;
        }

        public void setTotalAmountPaid(BigDecimal totalAmountPaid) {
            this.totalAmountPaid = totalAmountPaid;
        }

        public LocalDateTime getNextExpiryDate() {
            return nextExpiryDate;
        }

        public void setNextExpiryDate(LocalDateTime nextExpiryDate) {
            this.nextExpiryDate = nextExpiryDate;
        }

        public String getNextExpiryVehicle() {
            return nextExpiryVehicle;
        }

        public void setNextExpiryVehicle(String nextExpiryVehicle) {
            this.nextExpiryVehicle = nextExpiryVehicle;
        }
    }
}
