package nckh.felix.StupidParking.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import nckh.felix.StupidParking.domain.DangKyThang;

@Repository
public interface DangKyThangRepository extends JpaRepository<DangKyThang, Long> {

        // Tìm đăng ký tháng còn hiệu lực của một xe (lấy mới nhất nếu có nhiều)
        @Query(value = "SELECT * FROM dang_ky_thang WHERE bien_so_xe = :bienSoXe AND trang_thai = 'ACTIVE' AND thoi_gian_het_han > :currentTime ORDER BY thoi_gian_het_han DESC LIMIT 1", nativeQuery = true)
        Optional<DangKyThang> findActiveDangKyThangByBienSoXe(@Param("bienSoXe") String bienSoXe,
                        @Param("currentTime") LocalDateTime currentTime);

        // Tìm tất cả đăng ký tháng của một xe
        List<DangKyThang> findByBienSoXeOrderByCreatedDateDesc(String bienSoXe);

        // Tìm đăng ký tháng theo CCCD
        List<DangKyThang> findByCccdOrderByCreatedDateDesc(String cccd);

        // Tìm đăng ký tháng đã hết hạn để cập nhật trạng thái
        @Query("SELECT d FROM DangKyThang d WHERE d.trangThai = 'ACTIVE' AND d.thoiGianHetHan < :currentTime")
        List<DangKyThang> findExpiredDangKyThang(@Param("currentTime") LocalDateTime currentTime);

        // Kiểm tra xem xe có đăng ký tháng còn hiệu lực không
        @Query("SELECT COUNT(d) > 0 FROM DangKyThang d WHERE d.bienSoXe = :bienSoXe AND d.trangThai = 'ACTIVE' AND d.thoiGianHetHan > :currentTime")
        boolean hasActiveDangKyThang(@Param("bienSoXe") String bienSoXe,
                        @Param("currentTime") LocalDateTime currentTime);

        // Tìm đăng ký tháng theo nhân viên tạo
        List<DangKyThang> findByNhanVienTaoMaNVOrderByCreatedDateDesc(String maNV);

        // Tìm đăng ký tháng theo loại xe
        List<DangKyThang> findByLoaiXeMaLoaiXeOrderByCreatedDateDesc(String maLoaiXe);

        // Tìm đăng ký tháng trong khoảng thời gian
        @Query("SELECT d FROM DangKyThang d WHERE d.thoiGianBatDau >= :startDate AND d.thoiGianBatDau <= :endDate ORDER BY d.createdDate DESC")
        List<DangKyThang> findByThoiGianBatDauBetween(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // Tìm đăng ký tháng theo trạng thái
        List<DangKyThang> findByTrangThaiOrderByCreatedDateDesc(DangKyThang.TrangThaiDangKy trangThai);

        // Kiểm tra xem có yêu cầu gia hạn PENDING nào cho parentId này không
        boolean existsByParentIdAndTrangThai(Long parentId, DangKyThang.TrangThaiDangKy trangThai);

        // ============== VALIDATION METHODS ==============

        // Kiểm tra CCCD đã có đăng ký tháng ACTIVE chưa
        @Query("SELECT COUNT(d) > 0 FROM DangKyThang d WHERE d.cccd = :cccd AND d.trangThai = 'ACTIVE' AND d.thoiGianHetHan > :currentTime")
        boolean existsActiveDangKyThangByCccd(@Param("cccd") String cccd,
                        @Param("currentTime") LocalDateTime currentTime);

        // Kiểm tra số cavet đã có đăng ký tháng ACTIVE chưa
        @Query("SELECT COUNT(d) > 0 FROM DangKyThang d WHERE d.soCavet = :soCavet AND d.trangThai = 'ACTIVE' AND d.thoiGianHetHan > :currentTime")
        boolean existsActiveDangKyThangBySoCavet(@Param("soCavet") String soCavet,
                        @Param("currentTime") LocalDateTime currentTime);

        // Kiểm tra biển số xe đã có đăng ký tháng ACTIVE chưa
        @Query("SELECT COUNT(d) > 0 FROM DangKyThang d WHERE d.bienSoXe = :bienSoXe AND d.trangThai = 'ACTIVE' AND d.thoiGianHetHan > :currentTime")
        boolean existsActiveDangKyThangByBienSoXe(@Param("bienSoXe") String bienSoXe,
                        @Param("currentTime") LocalDateTime currentTime);

        // Tìm đăng ký tháng ACTIVE theo CCCD
        @Query("SELECT d FROM DangKyThang d WHERE d.cccd = :cccd AND d.trangThai = 'ACTIVE' AND d.thoiGianHetHan > :currentTime")
        List<DangKyThang> findActiveDangKyThangByCccd(@Param("cccd") String cccd,
                        @Param("currentTime") LocalDateTime currentTime);

        // Tìm đăng ký tháng ACTIVE theo số cavet
        @Query("SELECT d FROM DangKyThang d WHERE d.soCavet = :soCavet AND d.trangThai = 'ACTIVE' AND d.thoiGianHetHan > :currentTime")
        List<DangKyThang> findActiveDangKyThangBySoCavet(@Param("soCavet") String soCavet,
                        @Param("currentTime") LocalDateTime currentTime);

        // ============== EXTENSION CHAIN METHODS ==============

        // Tìm toàn bộ chuỗi gia hạn (root + all extensions) theo root ID
        @Query("SELECT d FROM DangKyThang d WHERE d.id = :rootId OR d.parentId = :rootId ORDER BY d.lanGiaHan ASC, d.createdDate ASC")
        List<DangKyThang> findExtensionChain(@Param("rootId") Long rootId);

        // Tìm tất cả extensions của một đăng ký root
        @Query("SELECT d FROM DangKyThang d WHERE d.parentId = :rootId ORDER BY d.lanGiaHan ASC, d.createdDate ASC")
        List<DangKyThang> findExtensionsByRoot(@Param("rootId") Long rootId);

        // Tìm đăng ký root (parentId is null) theo biển số xe
        @Query("SELECT d FROM DangKyThang d WHERE d.bienSoXe = :bienSoXe AND d.parentId IS NULL ORDER BY d.createdDate DESC")
        List<DangKyThang> findRootRegistrationsByBienSoXe(@Param("bienSoXe") String bienSoXe);

        // Tìm đăng ký theo parentId
        @Query("SELECT d FROM DangKyThang d WHERE d.parentId = :parentId ORDER BY d.createdDate ASC")
        List<DangKyThang> findByParentId(@Param("parentId") Long parentId);
}
