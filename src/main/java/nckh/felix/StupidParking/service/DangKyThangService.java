package nckh.felix.StupidParking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nckh.felix.StupidParking.domain.DangKyThang;
import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.domain.Vehicle;
import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.domain.dto.DangKyThangCreateDTO;
import nckh.felix.StupidParking.repository.DangKyThangRepository;
import nckh.felix.StupidParking.util.error.IdInvalidException;

@Service
public class DangKyThangService {

    private final DangKyThangRepository dangKyThangRepository;
    private final VehicleService vehicleService;
    private final StaffService staffService;
    private final VehicleTypeService vehicleTypeService;
    private final UserService userService;
    private final PriceService priceService;
    private final DangKyThangValidationService validationService;

    public DangKyThangService(DangKyThangRepository dangKyThangRepository,
            VehicleService vehicleService,
            StaffService staffService,
            VehicleTypeService vehicleTypeService,
            UserService userService,
            PriceService priceService,
            DangKyThangValidationService validationService) {
        this.dangKyThangRepository = dangKyThangRepository;
        this.vehicleService = vehicleService;
        this.staffService = staffService;
        this.vehicleTypeService = vehicleTypeService;
        this.userService = userService;
        this.priceService = priceService;
        this.validationService = validationService;
    }

    @Transactional
    public DangKyThang handleCreateDangKyThang(DangKyThangCreateDTO createDTO) throws IdInvalidException {
        // 1. VALIDATION TOÀN DIỆN
        try {
            validationService.validateCreateDangKyThang(createDTO);
        } catch (IllegalArgumentException e) {
            throw new IdInvalidException("Lỗi validation: " + e.getMessage());
        }

        // 2. Kiểm tra nhân viên có tồn tại và có quyền tạo đăng ký không
        Staff staff = staffService.fetchStaffByMaNV(createDTO.getMaNhanVien());
        if (staff == null) {
            throw new IdInvalidException("Nhân viên với mã " + createDTO.getMaNhanVien() + " không tồn tại");
        }

        // Kiểm tra quyền: chỉ Admin và Bảo vệ mới được tạo đăng ký tháng
        if (!staff.isAdmin() && !staff.isBaoVe()) {
            throw new IdInvalidException("Nhân viên không có quyền tạo đăng ký tháng");
        }

        // Kiểm tra loại xe có tồn tại không
        VehicleType vehicleType = vehicleTypeService.fetchVehicleTypeByMaLoaiXe(createDTO.getMaLoaiXe());
        if (vehicleType == null) {
            throw new IdInvalidException("Loại xe với mã " + createDTO.getMaLoaiXe() + " không tồn tại");
        }

        // Kiểm tra xe đã có đăng ký tháng còn hiệu lực chưa
        boolean hasActiveRegistration = hasActiveDangKyThang(createDTO.getBienSoXe());
        if (hasActiveRegistration) {
            throw new IdInvalidException(
                    "Xe với biển số " + createDTO.getBienSoXe() + " đã có đăng ký tháng còn hiệu lực");
        }

        // Tạo hoặc cập nhật User từ thông tin CCCD với email và password tùy chọn
        String defaultName = "Chủ xe " + createDTO.getBienSoXe();
        User user = userService.createOrUpdateUserFromMonthlyRegistration(
                createDTO.getCccd(),
                defaultName,
                createDTO.getDiaChi(),
                createDTO.getSoDienThoai(),
                createDTO.getEmail(),
                createDTO.getPassword());

        // Tạo hoặc lấy Vehicle (tạo mới nếu chưa tồn tại)
        Vehicle vehicle = vehicleService.fetchVehicleByBienSoXe(createDTO.getBienSoXe());
        if (vehicle == null) {
            // Tạo Vehicle mới nếu chưa tồn tại
            vehicle = new Vehicle();
            vehicle.setBienSoXe(createDTO.getBienSoXe());
            vehicle.setTenXe(createDTO.getTenXe());
            vehicle.setMaLoaiXe(vehicleType);
            vehicle.setOwner(user);
            vehicle = vehicleService.handleCreateVehicle(vehicle);
        } else {
            // Cập nhật owner cho Vehicle hiện có nếu cần
            if (vehicle.getOwner() == null ||
                    !Long.valueOf(vehicle.getOwner().getId()).equals(user.getId())) {
                vehicle.setOwner(user);
                vehicle = vehicleService.handleUpdateVehicle(vehicle);
            }
        }

        // Tạo đăng ký tháng mới
        DangKyThang dangKyThang = new DangKyThang();
        dangKyThang.setBienSoXe(createDTO.getBienSoXe());
        dangKyThang.setVehicle(vehicle);
        dangKyThang.setNhanVienTao(staff);
        dangKyThang.setSoThang(createDTO.getSoThang());
        dangKyThang.setCccd(createDTO.getCccd());
        dangKyThang.setSoCavet(createDTO.getSoCavet());
        dangKyThang.setDiaChi(createDTO.getDiaChi());
        dangKyThang.setLoaiXe(vehicleType);
        dangKyThang.setGhiChu(createDTO.getGhiChu());

        // Tính thời gian bắt đầu và hết hạn
        LocalDateTime thoiGianBatDau = LocalDateTime.now();
        LocalDateTime thoiGianHetHan = thoiGianBatDau.plusMonths(createDTO.getSoThang());

        dangKyThang.setThoiGianBatDau(thoiGianBatDau);
        dangKyThang.setThoiGianHetHan(thoiGianHetHan);

        // Tính số tiền thanh toán dựa trên Price, ParkingMode, VehicleType và số tháng
        BigDecimal soTienThanhToan = priceService.calculateMonthlyPrice(
                createDTO.getMaLoaiXe(),
                createDTO.getSoThang());
        dangKyThang.setSoTienThanhToan(soTienThanhToan);

        return dangKyThangRepository.save(dangKyThang);
    }

    public List<DangKyThang> getAllDangKyThang() {
        return dangKyThangRepository.findAll();
    }

    public DangKyThang fetchDangKyThangById(Long id) {
        Optional<DangKyThang> dangKyThangOptional = dangKyThangRepository.findById(id);
        return dangKyThangOptional.orElse(null);
    }

    public List<DangKyThang> getDangKyThangByBienSoXe(String bienSoXe) {
        return dangKyThangRepository.findByBienSoXeOrderByCreatedDateDesc(bienSoXe);
    }

    public List<DangKyThang> getDangKyThangByCccd(String cccd) {
        return dangKyThangRepository.findByCccdOrderByCreatedDateDesc(cccd);
    }

    public List<DangKyThang> getDangKyThangByNhanVien(String maNV) {
        return dangKyThangRepository.findByNhanVienTaoMaNVOrderByCreatedDateDesc(maNV);
    }

    @Transactional
    public DangKyThang handleUpdateDangKyThang(Long id, DangKyThangCreateDTO updateDTO) throws IdInvalidException {
        DangKyThang existingDangKyThang = fetchDangKyThangById(id);
        if (existingDangKyThang == null) {
            throw new IdInvalidException("Đăng ký tháng với ID " + id + " không tồn tại");
        }

        // Chỉ cho phép cập nhật nếu đăng ký chưa hết hạn
        if (existingDangKyThang.isExpired()) {
            throw new IdInvalidException("Không thể cập nhật đăng ký tháng đã hết hạn");
        }

        // Kiểm tra nhân viên có quyền cập nhật không
        Staff staff = staffService.fetchStaffByMaNV(updateDTO.getMaNhanVien());
        if (staff == null || (!staff.isAdmin() && !staff.isBaoVe())) {
            throw new IdInvalidException("Nhân viên không có quyền cập nhật đăng ký tháng");
        }

        // Cập nhật thông tin
        existingDangKyThang.setCccd(updateDTO.getCccd());
        existingDangKyThang.setSoCavet(updateDTO.getSoCavet());
        existingDangKyThang.setDiaChi(updateDTO.getDiaChi());
        existingDangKyThang.setGhiChu(updateDTO.getGhiChu());

        return dangKyThangRepository.save(existingDangKyThang);
    }

    @Transactional
    public DangKyThang handleExtendDangKyThang(Long id, Integer soThangMoi, String maNhanVien)
            throws IdInvalidException {
        DangKyThang existingDangKyThang = fetchDangKyThangById(id);
        if (existingDangKyThang == null) {
            throw new IdInvalidException("Đăng ký tháng với ID " + id + " không tồn tại");
        }

        // Kiểm tra nhân viên có quyền gia hạn không
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null || (!staff.isAdmin() && !staff.isBaoVe())) {
            throw new IdInvalidException("Nhân viên không có quyền gia hạn đăng ký tháng");
        }

        // Gia hạn từ thời điểm hết hạn hiện tại
        LocalDateTime newThoiGianHetHan = existingDangKyThang.getThoiGianHetHan().plusMonths(soThangMoi);
        existingDangKyThang.setThoiGianHetHan(newThoiGianHetHan);
        existingDangKyThang.setSoThang(existingDangKyThang.getSoThang() + soThangMoi);

        // Nếu đăng ký đã hết hạn, kích hoạt lại
        if (existingDangKyThang.isExpired()) {
            existingDangKyThang.setTrangThai(DangKyThang.TrangThaiDangKy.ACTIVE);
        }

        return dangKyThangRepository.save(existingDangKyThang);
    }

    @Transactional
    public void handleCancelDangKyThang(Long id, String maNhanVien) throws IdInvalidException {
        DangKyThang existingDangKyThang = fetchDangKyThangById(id);
        if (existingDangKyThang == null) {
            throw new IdInvalidException("Đăng ký tháng với ID " + id + " không tồn tại");
        }

        // Kiểm tra nhân viên có quyền hủy không (chỉ Admin)
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null || !staff.isAdmin()) {
            throw new IdInvalidException("Chỉ Admin mới có quyền hủy đăng ký tháng");
        }

        existingDangKyThang.cancel();
        dangKyThangRepository.save(existingDangKyThang);
    }

    @Transactional
    public DangKyThang handleRenewDangKyThang(String bienSoXe, Integer soThang, String maNhanVien)
            throws IdInvalidException {
        // Kiểm tra nhân viên có quyền tạo đăng ký không
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IdInvalidException("Nhân viên với mã " + maNhanVien + " không tồn tại");
        }

        if (!staff.isAdmin() && !staff.isBaoVe()) {
            throw new IdInvalidException("Nhân viên không có quyền tạo đăng ký tháng");
        }

        // Kiểm tra xe có tồn tại không
        Vehicle vehicle = vehicleService.fetchVehicleByBienSoXe(bienSoXe);
        if (vehicle == null) {
            throw new IdInvalidException("Xe với biển số " + bienSoXe + " không tồn tại");
        }

        // Kiểm tra xe đã có đăng ký tháng còn hiệu lực chưa
        boolean hasActiveRegistration = hasActiveDangKyThang(bienSoXe);
        if (hasActiveRegistration) {
            throw new IdInvalidException("Xe với biển số " + bienSoXe
                    + " đã có đăng ký tháng còn hiệu lực. Vui lòng sử dụng chức năng gia hạn thay vì tạo mới.");
        }

        // Lấy đăng ký tháng gần nhất (có thể đã hết hạn)
        List<DangKyThang> existingRegistrations = getDangKyThangByBienSoXe(bienSoXe);
        DangKyThang latestRegistration = null;
        if (!existingRegistrations.isEmpty()) {
            latestRegistration = existingRegistrations.get(0); // Đã sắp xếp theo ngày tạo DESC
        }

        // Tạo đăng ký tháng mới
        DangKyThang dangKyThang = new DangKyThang();
        dangKyThang.setBienSoXe(bienSoXe);
        dangKyThang.setVehicle(vehicle);
        dangKyThang.setNhanVienTao(staff);
        dangKyThang.setSoThang(soThang);
        dangKyThang.setLoaiXe(vehicle.getMaLoaiXe());

        // Sử dụng thông tin từ đăng ký cũ nếu có
        if (latestRegistration != null) {
            dangKyThang.setCccd(latestRegistration.getCccd());
            dangKyThang.setSoCavet(latestRegistration.getSoCavet());
            dangKyThang.setDiaChi(latestRegistration.getDiaChi());
            dangKyThang.setGhiChu("Gia hạn đăng ký tháng từ đăng ký cũ ID: " + latestRegistration.getId());
        } else {
            throw new IdInvalidException(
                    "Không tìm thấy đăng ký tháng cũ để gia hạn. Vui lòng tạo đăng ký mới với đầy đủ thông tin.");
        }

        // Tính thời gian bắt đầu và hết hạn
        LocalDateTime thoiGianBatDau = LocalDateTime.now();
        LocalDateTime thoiGianHetHan = thoiGianBatDau.plusMonths(soThang);

        dangKyThang.setThoiGianBatDau(thoiGianBatDau);
        dangKyThang.setThoiGianHetHan(thoiGianHetHan);

        return dangKyThangRepository.save(dangKyThang);
    }

    public List<DangKyThang> getDangKyThangByTrangThai(DangKyThang.TrangThaiDangKy trangThai) {
        return dangKyThangRepository.findByTrangThaiOrderByCreatedDateDesc(trangThai);
    }

    public List<DangKyThang> getDangKyThangInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return dangKyThangRepository.findByThoiGianBatDauBetween(startDate, endDate);
    }

    // ============== VALIDATION & BUSINESS LOGIC METHODS ==============

    /**
     * Kiểm tra xe có đăng ký tháng còn hiệu lực không
     */
    public boolean hasActiveDangKyThang(String bienSoXe) {
        return validationService.canExtendDangKyThang(bienSoXe, LocalDateTime.now());
    }

    /**
     * Lấy đăng ký tháng còn hiệu lực của xe
     */
    public DangKyThang getActiveDangKyThang(String bienSoXe) {
        return validationService.getActiveDangKyThang(bienSoXe, LocalDateTime.now());
    }

    /**
     * Hủy đăng ký tháng (chỉ hủy được nếu chưa hết hạn)
     */
    @Transactional
    public DangKyThang cancelDangKyThang(Long id, String maNhanVien) throws IdInvalidException {
        DangKyThang dangKyThang = fetchDangKyThangById(id);

        if (dangKyThang.getTrangThai() != DangKyThang.TrangThaiDangKy.ACTIVE) {
            throw new IdInvalidException("Chỉ có thể hủy đăng ký tháng đang hiệu lực");
        }

        if (dangKyThang.getThoiGianHetHan().isBefore(LocalDateTime.now())) {
            throw new IdInvalidException("Không thể hủy đăng ký tháng đã hết hạn");
        }

        // Kiểm tra nhân viên có quyền hủy không (có thể thêm logic phân quyền)
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IdInvalidException("Nhân viên không tồn tại");
        }

        dangKyThang.setTrangThai(DangKyThang.TrangThaiDangKy.CANCELLED);
        dangKyThang.setUpdatedDate(LocalDateTime.now());

        return dangKyThangRepository.save(dangKyThang);
    }

    /**
     * Gia hạn đăng ký tháng (extend thời gian hiện tại)
     */
    @Transactional
    public DangKyThang extendDangKyThang(Long id, Integer themSoThang, String maNhanVien) throws IdInvalidException {
        DangKyThang dangKyThang = fetchDangKyThangById(id);

        if (dangKyThang.getTrangThai() != DangKyThang.TrangThaiDangKy.ACTIVE) {
            throw new IdInvalidException("Chỉ có thể gia hạn đăng ký tháng đang hiệu lực");
        }

        if (themSoThang == null || themSoThang < 1 || themSoThang > 12) {
            throw new IdInvalidException("Số tháng gia hạn phải từ 1 đến 12");
        }

        // Kiểm tra nhân viên
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IdInvalidException("Nhân viên không tồn tại");
        }

        // Tính giá gia hạn
        BigDecimal giaGiaHan = priceService.calculateMonthlyPrice(
                dangKyThang.getLoaiXe().getMaLoaiXe(),
                themSoThang);

        // Cập nhật thời gian hết hạn và số tiền
        dangKyThang.setThoiGianHetHan(dangKyThang.getThoiGianHetHan().plusMonths(themSoThang));
        dangKyThang.setSoThang(dangKyThang.getSoThang() + themSoThang);
        dangKyThang.setSoTienThanhToan(dangKyThang.getSoTienThanhToan().add(giaGiaHan));
        dangKyThang.setUpdatedDate(LocalDateTime.now());
        dangKyThang.setGhiChu(dangKyThang.getGhiChu() + " | Gia hạn " + themSoThang + " tháng bởi " + maNhanVien);

        return dangKyThangRepository.save(dangKyThang);
    }

    /**
     * Cập nhật trạng thái đăng ký tháng đã hết hạn (chạy định kỳ)
     */
    @Transactional
    public void updateExpiredDangKyThang() {
        List<DangKyThang> expiredList = dangKyThangRepository.findExpiredDangKyThang(LocalDateTime.now());

        for (DangKyThang dangKy : expiredList) {
            dangKy.setTrangThai(DangKyThang.TrangThaiDangKy.EXPIRED);
            dangKy.setUpdatedDate(LocalDateTime.now());
            dangKyThangRepository.save(dangKy);
        }
    }
}
