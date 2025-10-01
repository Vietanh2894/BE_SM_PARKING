package nckh.felix.StupidParking.domain.dto;

/**
 * Response DTO thống nhất cho unified login
 * Có thể chứa thông tin User hoặc Staff tùy theo loại tài khoản
 */
public class UnifiedLoginResponseDTO {
    private String accessToken;
    private String userType; // "USER" hoặc "STAFF"
    private UserInfo userInfo;
    private StaffInfo staffInfo;

    // Constructors
    public UnifiedLoginResponseDTO() {
    }

    // Constructor cho User login
    public UnifiedLoginResponseDTO(String accessToken, UserInfo userInfo) {
        this.accessToken = accessToken;
        this.userType = "USER";
        this.userInfo = userInfo;
    }

    // Constructor cho Staff login
    public UnifiedLoginResponseDTO(String accessToken, StaffInfo staffInfo) {
        this.accessToken = accessToken;
        this.userType = "STAFF";
        this.staffInfo = staffInfo;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public StaffInfo getStaffInfo() {
        return staffInfo;
    }

    public void setStaffInfo(StaffInfo staffInfo) {
        this.staffInfo = staffInfo;
    }

    // Inner class cho thông tin User
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String role;

        // Constructors
        public UserInfo() {
        }

        public UserInfo(Long id, String name, String email, String role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
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

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    // Inner class cho thông tin Staff
    public static class StaffInfo {
        private String maNV;
        private String hoTen;
        private String email;
        private String chucVu;

        // Constructors
        public StaffInfo() {
        }

        public StaffInfo(String maNV, String hoTen, String email, String chucVu) {
            this.maNV = maNV;
            this.hoTen = hoTen;
            this.email = email;
            this.chucVu = chucVu;
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

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getChucVu() {
            return chucVu;
        }

        public void setChucVu(String chucVu) {
            this.chucVu = chucVu;
        }
    }
}