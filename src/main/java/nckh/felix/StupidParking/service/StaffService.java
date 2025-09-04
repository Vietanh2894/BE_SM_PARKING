package nckh.felix.StupidParking.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.Account;
import nckh.felix.StupidParking.domain.Staff.ChucVu;
import nckh.felix.StupidParking.repository.StaffRepository;

@Service
public class StaffService {
    private StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public Staff handleCreateStaff(Staff staff) {
        // Validation: kiểm tra email và CCCD đã tồn tại chưa
        if (this.staffRepository.findByEmail(staff.getEmail()) != null) {
            throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
        }
        if (this.staffRepository.findByCccd(staff.getCccd()) != null) {
            throw new IllegalArgumentException("CCCD đã tồn tại trong hệ thống");
        }
        if (this.staffRepository.findBySdt(staff.getSdt()) != null) {
            throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống");
        }
        return this.staffRepository.save(staff);
    }

    public Staff fetchStaffByMaNV(String maNV) {
        Optional<Staff> staffOptional = this.staffRepository.findById(maNV);
        if (staffOptional.isPresent()) {
            return staffOptional.get();
        }
        return null;
    }

    public List<Staff> fetchAllStaff() {
        return this.staffRepository.findAll();
    }

    public Staff handleUpdateStaff(Staff reqStaff) {
        Staff currentStaff = this.fetchStaffByMaNV(reqStaff.getMaNV());
        if (currentStaff != null) {
            // Validation: kiểm tra email và CCCD đã tồn tại chưa (trừ chính nó)
            Staff existingEmailStaff = this.staffRepository.findByEmail(reqStaff.getEmail());
            if (existingEmailStaff != null && !existingEmailStaff.getMaNV().equals(reqStaff.getMaNV())) {
                throw new IllegalArgumentException("Email đã tồn tại trong hệ thống");
            }

            Staff existingCccdStaff = this.staffRepository.findByCccd(reqStaff.getCccd());
            if (existingCccdStaff != null && !existingCccdStaff.getMaNV().equals(reqStaff.getMaNV())) {
                throw new IllegalArgumentException("CCCD đã tồn tại trong hệ thống");
            }

            Staff existingSdtStaff = this.staffRepository.findBySdt(reqStaff.getSdt());
            if (existingSdtStaff != null && !existingSdtStaff.getMaNV().equals(reqStaff.getMaNV())) {
                throw new IllegalArgumentException("Số điện thoại đã tồn tại trong hệ thống");
            }

            currentStaff.setHoTen(reqStaff.getHoTen());
            currentStaff.setSdt(reqStaff.getSdt());
            currentStaff.setEmail(reqStaff.getEmail());
            currentStaff.setCccd(reqStaff.getCccd());
            currentStaff.setChucVu(reqStaff.getChucVu());
            currentStaff.setNgayVaoLam(reqStaff.getNgayVaoLam());
            currentStaff.setAccount(reqStaff.getAccount());
            currentStaff = this.staffRepository.save(currentStaff);
        }
        return currentStaff;
    }

    public void handleDeleteStaff(String maNV) {
        this.staffRepository.deleteById(maNV);
    }

    // Business logic methods
    public List<Staff> fetchStaffByChucVu(ChucVu chucVu) {
        return this.staffRepository.findByChucVu(chucVu);
    }

    public List<Staff> fetchActiveStaffByChucVu(ChucVu chucVu) {
        return this.staffRepository.findActiveStaffByChucVu(chucVu);
    }

    public List<Staff> fetchStaffWithActiveAccount() {
        return this.staffRepository.findStaffWithActiveAccount();
    }

    public Staff fetchStaffByAccount(Account account) {
        return this.staffRepository.findByAccount(account);
    }

    public Staff fetchStaffByUsername(String username) {
        return this.staffRepository.findByAccountUsername(username);
    }

    public Staff fetchStaffByEmail(String email) {
        return this.staffRepository.findByEmail(email);
    }

    public Staff fetchStaffByCccd(String cccd) {
        return this.staffRepository.findByCccd(cccd);
    }

    public Staff fetchStaffBySdt(String sdt) {
        return this.staffRepository.findBySdt(sdt);
    }

    public List<Staff> searchStaffByHoTen(String hoTen) {
        return this.staffRepository.findByHoTenContaining(hoTen);
    }

    public List<Staff> fetchStaffByNgayVaoLam(LocalDate startDate, LocalDate endDate) {
        return this.staffRepository.findByNgayVaoLamBetween(startDate, endDate);
    }

    // Statistics methods
    public Long countStaffByChucVu(ChucVu chucVu) {
        return this.staffRepository.countByChucVu(chucVu);
    }

    // Permission check methods
    public boolean canStaffManageStaffAndAccount(String maNV) {
        Staff staff = this.fetchStaffByMaNV(maNV);
        return staff != null && staff.canManageStaffAndAccount();
    }

    public boolean isStaffAdmin(String maNV) {
        Staff staff = this.fetchStaffByMaNV(maNV);
        return staff != null && staff.isAdmin();
    }

    public boolean isStaffBaoVe(String maNV) {
        Staff staff = this.fetchStaffByMaNV(maNV);
        return staff != null && staff.isBaoVe();
    }
}
