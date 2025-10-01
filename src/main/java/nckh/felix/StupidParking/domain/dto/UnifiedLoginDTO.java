package nckh.felix.StupidParking.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO thống nhất cho việc login cả User và Staff
 * - Có thể nhận email (cho User) hoặc username (cho Staff)
 * - Backend sẽ tự động phát hiện loại tài khoản
 */
public class UnifiedLoginDTO {
    @NotBlank(message = "Username/Email không được để trống")
    @Size(max = 150, message = "Username/Email không được vượt quá 150 ký tự")
    private String credential; // Có thể là email hoặc username

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải có ít nhất 6 ký tự")
    private String password;

    // Constructors
    public UnifiedLoginDTO() {
    }

    public UnifiedLoginDTO(String credential, String password) {
        this.credential = credential;
        this.password = password;
    }

    // Getters and Setters
    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Kiểm tra credential có phải là email không
     * 
     * @return true nếu credential chứa ký tự '@'
     */
    public boolean isEmail() {
        return credential != null && credential.contains("@");
    }

    /**
     * Kiểm tra credential có phải là username không
     * 
     * @return true nếu credential không chứa ký tự '@'
     */
    public boolean isUsername() {
        return credential != null && !credential.contains("@");
    }

    @Override
    public String toString() {
        return "UnifiedLoginDTO{" +
                "credential='" + credential + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}