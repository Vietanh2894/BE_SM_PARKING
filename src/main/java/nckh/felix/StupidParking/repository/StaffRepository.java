package nckh.felix.StupidParking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.Account;
import nckh.felix.StupidParking.domain.Staff.ChucVu;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StaffRepository extends JpaRepository<Staff, String> {
    // JpaRepository sẽ tự động cung cấp các phương thức CRUD với String làm ID
    // findById, save, delete, findAll, etc.

    // Tìm staff theo chức vụ
    List<Staff> findByChucVu(ChucVu chucVu);

    // Tìm staff theo account
    Staff findByAccount(Account account);

    // Tìm staff theo username của account
    @Query("SELECT s FROM Staff s WHERE s.account.username = :username")
    Staff findByAccountUsername(@Param("username") String username);

    // Tìm staff theo email
    Staff findByEmail(String email);

    // Tìm staff theo CCCD
    Staff findByCccd(String cccd);

    // Tìm staff theo số điện thoại
    Staff findBySdt(String sdt);

    // Tìm staff có account active
    @Query("SELECT s FROM Staff s WHERE s.account.trangThai = 'ENABLE'")
    List<Staff> findStaffWithActiveAccount();

    // Tìm staff theo chức vụ và account active
    @Query("SELECT s FROM Staff s WHERE s.chucVu = :chucVu AND s.account.trangThai = 'ENABLE'")
    List<Staff> findActiveStaffByChucVu(@Param("chucVu") ChucVu chucVu);

    // Tìm staff theo tên (like search)
    @Query("SELECT s FROM Staff s WHERE s.hoTen LIKE %:hoTen%")
    List<Staff> findByHoTenContaining(@Param("hoTen") String hoTen);

    // Tìm staff vào làm trong khoảng thời gian
    @Query("SELECT s FROM Staff s WHERE s.ngayVaoLam BETWEEN :startDate AND :endDate")
    List<Staff> findByNgayVaoLamBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Đếm số lượng staff theo chức vụ
    @Query("SELECT COUNT(s) FROM Staff s WHERE s.chucVu = :chucVu")
    Long countByChucVu(@Param("chucVu") ChucVu chucVu);
}
