
package nckh.felix.StupidParking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "Xe")
public class Vehicle {
    @Id
    @Column(name = "BienSoXe", length = 20, columnDefinition = "nchar(20)")
    private String bienSoXe;

    @Column(name = "TenXe", length = 100)
    private String tenXe;

    // nhiều xe có thể thuộc về một loại xe
    @ManyToOne
    @JoinColumn(name = "MaLoaiXe", nullable = false)
    private VehicleType maLoaiXe;

    @Column(name = "CreatedDate", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Id", nullable = true)
    private User owner;

    // Default constructor
    public Vehicle() {
        this.createdDate = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public String getBienSoXe() {
        return bienSoXe;
    }

    public void setBienSoXe(String bienSoXe) {
        this.bienSoXe = bienSoXe;
    }

    public VehicleType getMaLoaiXe() {
        return maLoaiXe;
    }

    public void setMaLoaiXe(VehicleType maLoaiXe) {
        this.maLoaiXe = maLoaiXe;
    }

    public String getTenXe() {
        return tenXe;
    }

    public void setTenXe(String tenXe) {
        this.tenXe = tenXe;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Vehicle [bienSoXe=" + bienSoXe + ", tenXe=" + tenXe + ", maLoaiXe=" + maLoaiXe + ", createdDate="
                + createdDate + ", owner=" + owner + "]";
    }

}