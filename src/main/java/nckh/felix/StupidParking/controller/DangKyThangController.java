package nckh.felix.StupidParking.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import nckh.felix.StupidParking.domain.DangKyThang;
import nckh.felix.StupidParking.domain.dto.DangKyThangCreateDTO;
import nckh.felix.StupidParking.domain.dto.DangKyThangUpdateDTO;
import nckh.felix.StupidParking.service.DangKyThangService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

@RestController
public class DangKyThangController {

    private final DangKyThangService dangKyThangService;

    public DangKyThangController(DangKyThangService dangKyThangService) {
        this.dangKyThangService = dangKyThangService;
    }

    /**
     * Tạo đăng ký tháng mới
     * Chỉ Staff (Admin và Bảo vệ) mới có quyền tạo
     */
    @PostMapping("/dang-ky-thang")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('BAO_VE')")
    public ResponseEntity<?> createDangKyThang(@Valid @RequestBody DangKyThangCreateDTO createDTO) {
        try {
            DangKyThang dangKyThang = dangKyThangService.handleCreateDangKyThang(createDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(dangKyThang);
        } catch (IdInvalidException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Lấy thông tin đăng ký tháng theo ID
     */
    @GetMapping("/dang-ky-thang/{id}")
    public ResponseEntity<DangKyThang> getDangKyThangById(@PathVariable("id") Long id) {
        DangKyThang dangKyThang = dangKyThangService.fetchDangKyThangById(id);
        if (dangKyThang != null) {
            return ResponseEntity.ok(dangKyThang);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy tất cả đăng ký tháng
     */
    @GetMapping("/dang-ky-thang")
    public ResponseEntity<List<DangKyThang>> getAllDangKyThang() {
        List<DangKyThang> dangKyThangs = dangKyThangService.getAllDangKyThang();
        return ResponseEntity.ok(dangKyThangs);
    }

    /**
     * Lấy đăng ký tháng theo biển số xe
     */
    @GetMapping("/dang-ky-thang/bien-so-xe/{bienSoXe}")
    public ResponseEntity<List<DangKyThang>> getDangKyThangByBienSoXe(@PathVariable("bienSoXe") String bienSoXe) {
        List<DangKyThang> dangKyThangs = dangKyThangService.getDangKyThangByBienSoXe(bienSoXe);
        return ResponseEntity.ok(dangKyThangs);
    }

    /**
     * Kiểm tra xe có đăng ký tháng còn hiệu lực không
     */
    @GetMapping("/dang-ky-thang/check-active/{bienSoXe}")
    public ResponseEntity<Boolean> checkActiveDangKyThang(@PathVariable("bienSoXe") String bienSoXe) {
        boolean hasActive = dangKyThangService.hasActiveDangKyThang(bienSoXe);
        return ResponseEntity.ok(hasActive);
    }

    /**
     * Lấy đăng ký tháng còn hiệu lực của xe
     */
    @GetMapping("/dang-ky-thang/active/{bienSoXe}")
    public ResponseEntity<DangKyThang> getActiveDangKyThang(@PathVariable("bienSoXe") String bienSoXe) {
        DangKyThang activeDangKy = dangKyThangService.getActiveDangKyThang(bienSoXe);
        if (activeDangKy != null) {
            return ResponseEntity.ok(activeDangKy);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lấy đăng ký tháng theo CCCD
     */
    @GetMapping("/dang-ky-thang/cccd/{cccd}")
    public ResponseEntity<List<DangKyThang>> getDangKyThangByCccd(@PathVariable("cccd") String cccd) {
        List<DangKyThang> dangKyThangs = dangKyThangService.getDangKyThangByCccd(cccd);
        return ResponseEntity.ok(dangKyThangs);
    }

    /**
     * Lấy đăng ký tháng theo nhân viên tạo
     */
    @GetMapping("/dang-ky-thang/nhan-vien/{maNV}")
    public ResponseEntity<List<DangKyThang>> getDangKyThangByNhanVien(@PathVariable("maNV") String maNV) {
        List<DangKyThang> dangKyThangs = dangKyThangService.getDangKyThangByNhanVien(maNV);
        return ResponseEntity.ok(dangKyThangs);
    }

    /**
     * Lấy đăng ký tháng theo trạng thái
     */
    @GetMapping("/dang-ky-thang/trang-thai/{trangThai}")
    public ResponseEntity<List<DangKyThang>> getDangKyThangByTrangThai(@PathVariable("trangThai") String trangThai) {
        try {
            DangKyThang.TrangThaiDangKy status = DangKyThang.TrangThaiDangKy.valueOf(trangThai.toUpperCase());
            List<DangKyThang> dangKyThangs = dangKyThangService.getDangKyThangByTrangThai(status);
            return ResponseEntity.ok(dangKyThangs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Cập nhật số tháng đăng ký (chỉ cho phép giảm) và tính lại tiền
     * Để cập nhật thông tin cá nhân/xe, vui lòng sử dụng API User/Vehicle
     */
    @PutMapping("/dang-ky-thang/{id}")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('BAO_VE')")
    public ResponseEntity<?> updateDangKyThang(@PathVariable("id") Long id,
            @Valid @RequestBody DangKyThangUpdateDTO updateDTO) {
        try {
            DangKyThang updatedDangKyThang = dangKyThangService.handleUpdateDangKyThang(id, updateDTO);
            return ResponseEntity.ok(updatedDangKyThang);
        } catch (IdInvalidException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Gia hạn đăng ký tháng (nối tiếp thời gian hết hạn hiện tại)
     */
    @PutMapping("/dang-ky-thang/{id}/extend")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('BAO_VE')")
    public ResponseEntity<?> extendDangKyThang(@PathVariable("id") Long id,
            @RequestParam("soThangMoi") Integer soThangMoi,
            @RequestParam("maNhanVien") String maNhanVien) {
        try {
            DangKyThang extendedDangKyThang = dangKyThangService.handleExtendDangKyThang(id, soThangMoi, maNhanVien);
            return ResponseEntity.ok(extendedDangKyThang);
        } catch (IdInvalidException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Gia hạn đăng ký tháng (tạo mới cho xe đã hết hạn)
     */
    @PostMapping("/dang-ky-thang/renew")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('BAO_VE')")
    public ResponseEntity<?> renewDangKyThang(@RequestParam("bienSoXe") String bienSoXe,
            @RequestParam("soThang") Integer soThang,
            @RequestParam("maNhanVien") String maNhanVien) {
        try {
            DangKyThang renewedDangKyThang = dangKyThangService.handleRenewDangKyThang(bienSoXe, soThang, maNhanVien);
            return ResponseEntity.status(HttpStatus.CREATED).body(renewedDangKyThang);
        } catch (IdInvalidException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Hủy đăng ký tháng
     * Chỉ Admin mới có quyền hủy
     */
    @DeleteMapping("/dang-ky-thang/{id}")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cancelDangKyThang(@PathVariable("id") Long id,
            @RequestParam("maNhanVien") String maNhanVien) {
        try {
            dangKyThangService.handleCancelDangKyThang(id, maNhanVien);
            return ResponseEntity.ok("Hủy đăng ký tháng thành công");
        } catch (IdInvalidException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái đăng ký tháng hết hạn
     * API này có thể được gọi định kỳ để cập nhật trạng thái
     */
    @PutMapping("/dang-ky-thang/update-expired")
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateExpiredDangKyThang() {
        try {
            dangKyThangService.updateExpiredDangKyThang();
            return ResponseEntity.ok("Cập nhật trạng thái đăng ký tháng hết hạn thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Lấy đăng ký tháng trong khoảng thời gian
     */
    @GetMapping("/dang-ky-thang/date-range")
    public ResponseEntity<List<DangKyThang>> getDangKyThangInDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        try {
            LocalDateTime start = LocalDateTime.parse(startDate);
            LocalDateTime end = LocalDateTime.parse(endDate);
            List<DangKyThang> dangKyThangs = dangKyThangService.getDangKyThangInDateRange(start, end);
            return ResponseEntity.ok(dangKyThangs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
