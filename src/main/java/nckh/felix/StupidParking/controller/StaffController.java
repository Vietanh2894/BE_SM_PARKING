package nckh.felix.StupidParking.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.Account;
import nckh.felix.StupidParking.domain.Staff.ChucVu;
import nckh.felix.StupidParking.service.StaffService;
import nckh.felix.StupidParking.util.error.IdInvalidException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping("/staff")
    public ResponseEntity<Staff> createNewStaff(@RequestBody Staff xStaff) {
        try {
            Staff vStaff = this.staffService.handleCreateStaff(xStaff);
            return ResponseEntity.status(HttpStatus.CREATED).body(vStaff);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/staff/{maNV}")
    public ResponseEntity<Staff> getStaffByMaNV(@PathVariable("maNV") String maNV) {
        Staff vStaff = this.staffService.fetchStaffByMaNV(maNV);
        return ResponseEntity.status(HttpStatus.OK).body(vStaff);
    }

    @GetMapping("/staff")
    public ResponseEntity<List<Staff>> getAllStaff() {
        return ResponseEntity.status(HttpStatus.OK).body(this.staffService.fetchAllStaff());
    }

    @PutMapping("/staff")
    public ResponseEntity<Staff> updateStaff(@RequestBody Staff xStaff) {
        try {
            Staff vStaff = this.staffService.handleUpdateStaff(xStaff);
            return ResponseEntity.ok(vStaff);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/staff/{maNV}")
    public ResponseEntity<String> deleteStaff(@PathVariable("maNV") String maNV) throws IdInvalidException {
        this.staffService.handleDeleteStaff(maNV);
        return ResponseEntity.ok("Xóa nhân viên thành công");
    }

    // Business logic endpoints
    @GetMapping("/staff/chuc-vu/{chucVu}")
    public ResponseEntity<List<Staff>> getStaffByChucVu(@PathVariable("chucVu") String chucVu) {
        ChucVu position = ChucVu.valueOf(chucVu.toUpperCase());
        return ResponseEntity.ok(this.staffService.fetchStaffByChucVu(position));
    }

    @GetMapping("/staff/active/chuc-vu/{chucVu}")
    public ResponseEntity<List<Staff>> getActiveStaffByChucVu(@PathVariable("chucVu") String chucVu) {
        ChucVu position = ChucVu.valueOf(chucVu.toUpperCase());
        return ResponseEntity.ok(this.staffService.fetchActiveStaffByChucVu(position));
    }

    @GetMapping("/staff/active")
    public ResponseEntity<List<Staff>> getStaffWithActiveAccount() {
        return ResponseEntity.ok(this.staffService.fetchStaffWithActiveAccount());
    }

    @GetMapping("/staff/account/{username}")
    public ResponseEntity<Staff> getStaffByAccount(@PathVariable("username") String username) {
        Account account = new Account(username);
        Staff staff = this.staffService.fetchStaffByAccount(account);
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/staff/email/{email}")
    public ResponseEntity<Staff> getStaffByEmail(@PathVariable("email") String email) {
        Staff staff = this.staffService.fetchStaffByEmail(email);
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/staff/cccd/{cccd}")
    public ResponseEntity<Staff> getStaffByCccd(@PathVariable("cccd") String cccd) {
        Staff staff = this.staffService.fetchStaffByCccd(cccd);
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/staff/sdt/{sdt}")
    public ResponseEntity<Staff> getStaffBySdt(@PathVariable("sdt") String sdt) {
        Staff staff = this.staffService.fetchStaffBySdt(sdt);
        return ResponseEntity.ok(staff);
    }

    @GetMapping("/staff/search")
    public ResponseEntity<List<Staff>> searchStaffByHoTen(@RequestParam("name") String hoTen) {
        return ResponseEntity.ok(this.staffService.searchStaffByHoTen(hoTen));
    }

    @GetMapping("/staff/ngay-vao-lam")
    public ResponseEntity<List<Staff>> getStaffByNgayVaoLam(@RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return ResponseEntity.ok(this.staffService.fetchStaffByNgayVaoLam(start, end));
    }

    // Permission check endpoints
    @GetMapping("/staff/{maNV}/can-manage-staff-account")
    public ResponseEntity<Boolean> canStaffManageStaffAndAccount(@PathVariable("maNV") String maNV) {
        boolean canManage = this.staffService.canStaffManageStaffAndAccount(maNV);
        return ResponseEntity.ok(canManage);
    }

    @GetMapping("/staff/{maNV}/is-admin")
    public ResponseEntity<Boolean> isStaffAdmin(@PathVariable("maNV") String maNV) {
        boolean isAdmin = this.staffService.isStaffAdmin(maNV);
        return ResponseEntity.ok(isAdmin);
    }

    @GetMapping("/staff/{maNV}/is-bao-ve")
    public ResponseEntity<Boolean> isStaffBaoVe(@PathVariable("maNV") String maNV) {
        boolean isBaoVe = this.staffService.isStaffBaoVe(maNV);
        return ResponseEntity.ok(isBaoVe);
    }

    // Statistics endpoints
    @GetMapping("/staff/count/chuc-vu/{chucVu}")
    public ResponseEntity<Long> countStaffByChucVu(@PathVariable("chucVu") String chucVu) {
        ChucVu position = ChucVu.valueOf(chucVu.toUpperCase());
        return ResponseEntity.ok(this.staffService.countStaffByChucVu(position));
    }
}
