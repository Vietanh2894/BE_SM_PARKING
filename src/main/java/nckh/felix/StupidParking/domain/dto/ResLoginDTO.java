package nckh.felix.StupidParking.domain.dto;

public class ResLoginDTO {
    private String accessToken;
    private String userType; // "STAFF" cho login Staff
    private StaffInfo staffInfo;

    // Constructors
    public ResLoginDTO() {
    }

    public ResLoginDTO(String accessToken, String userType, StaffInfo staffInfo) {
        this.accessToken = accessToken;
        this.userType = userType;
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

    public StaffInfo getStaffInfo() {
        return staffInfo;
    }

    public void setStaffInfo(StaffInfo staffInfo) {
        this.staffInfo = staffInfo;
    }

    // Inner class cho thông tin staff
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
