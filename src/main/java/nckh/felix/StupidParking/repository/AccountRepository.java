package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.Account;
import nckh.felix.StupidParking.domain.Role;
import nckh.felix.StupidParking.domain.Account.TrangThaiAccount;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    // JpaRepository sẽ tự động cung cấp các phương thức CRUD với String làm ID
    // findById, save, delete, findAll, etc.

    // Tìm account theo trạng thái
    List<Account> findByTrangThai(TrangThaiAccount trangThai);

    // Tìm account theo role
    List<Account> findByRole(Role role);

    // Tìm account theo username và password (cho login)
    Account findByUsernameAndPassword(String username, String password);

    // Tìm account active theo role
    @Query("SELECT a FROM Account a WHERE a.trangThai = 'ENABLE' AND a.role = :role")
    List<Account> findActiveAccountsByRole(@Param("role") Role role);

    // Đếm số lượng account theo trạng thái
    @Query("SELECT COUNT(a) FROM Account a WHERE a.trangThai = :trangThai")
    Long countByTrangThai(@Param("trangThai") TrangThaiAccount trangThai);
}
