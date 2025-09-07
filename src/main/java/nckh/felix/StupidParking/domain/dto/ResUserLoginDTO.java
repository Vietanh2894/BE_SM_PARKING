package nckh.felix.StupidParking.domain.dto;

public class ResUserLoginDTO {
    private String accessToken;
    private String userType; // "USER" hoặc "STAFF"
    private UserInfo userInfo;

    // Constructors
    public ResUserLoginDTO() {
    }

    public ResUserLoginDTO(String accessToken, String userType, UserInfo userInfo) {
        this.accessToken = accessToken;
        this.userType = userType;
        this.userInfo = userInfo;
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

    // Inner class cho thông tin user
    public static class UserInfo {
        private Long id;
        private String name;
        private String email;
        private String role; // "USER" hoặc role của staff

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
}
