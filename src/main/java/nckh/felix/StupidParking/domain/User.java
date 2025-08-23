package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "name", nullable = false, length = 100)
    @NotBlank(message = "Tên không được để trống ")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    @NotBlank(message = "Email không được để trống ")
    @Email(message = "Email không hợp lệ")
    @Size(max = 150, message = "Email không được vượt quá 150 ký tự")
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank(message = "Password không được để trống ")
    @Size(min = 6, max = 255, message = "Password phải có độ dài từ 6 đến 255 ký tự")
    private String password;

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
