package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
    // JpaRepository sẽ tự động cung cấp các phương thức CRUD với String làm ID
    // findById, save, delete, findAll, etc.

    // Tìm role theo tên
    Role findByRoleName(String roleName);
}
