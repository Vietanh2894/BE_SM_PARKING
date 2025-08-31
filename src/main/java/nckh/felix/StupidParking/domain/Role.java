package nckh.felix.StupidParking.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @Column(name = "role_id", length = 10, nullable = false)
    @NotBlank(message = "Role ID không được để trống")
    @Size(max = 10, message = "Role ID không được vượt quá 10 ký tự")
    private String roleId;

    @Column(name = "role_name", nullable = false, length = 50)
    @NotBlank(message = "Tên role không được để trống")
    @Size(max = 50, message = "Tên role không được vượt quá 50 ký tự")
    private String roleName;

    // Default constructor
    public Role() {
    }

    // Constructor for JSON deserialization from String
    public Role(String roleId) {
        this.roleId = roleId;
    }

    // Constructor with parameters
    public Role(String roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return "Role [roleId=" + roleId + ", roleName=" + roleName + "]";
    }
}
