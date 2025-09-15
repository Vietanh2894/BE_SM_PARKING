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
import nckh.felix.StupidParking.domain.dto.DangKyThangUpdateDTO;
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

    public DangKyThangService(DangKyThangRepository dangKyThangRepository,
            VehicleService vehicleService,
            StaffService staffService,
            VehicleTypeService vehicleTypeService,
            UserService userService,
            PriceService priceService) {
        this.dangKyThangRepository = dangKyThangRepository;
        this.vehicleService = vehicleService;
        this.staffService = staffService;
        this.vehicleTypeService = vehicleTypeService;
        this.userService = userService;
        this.priceService = priceService;
    }

    @Transactional
    public DangKyThang handleCreateDangKyThang(DangKyThangCreateDTO createDTO) throws IdInvalidException {
        // 1. VALIDATION CƠ BẢN
        if (createDTO == null) {
            throw new IdInvalidException("Dữ liệu đăng ký không được để trống");
        }

        // 2. VALIDATION BUSINESS LOGIC - Uniqueness constraints (không có trong domain)
        LocalDateTime now = LocalDateTime.now();

        // Kiểm tra tính duy nhất của CCCD
        if (dangKyThangRepository.existsActiveDangKyThangByCccd(createDTO.getCccd(), now)) {
            throw new IdInvalidException("CCCD '" + createDTO.getCccd() + "' đã có đăng ký tháng còn hiệu lực");
        }

        // Kiểm tra tính duy nhất của số cavet
        if (dangKyThangRepository.existsActiveDangKyThangBySoCavet(createDTO.getSoCavet(), now)) {
            throw new IdInvalidException("Số cavet '" + createDTO.getSoCavet() + "' đã có đăng ký tháng còn hiệu lực");
        }

        // Kiểm tra tính duy nhất của biển số xe
        if (dangKyThangRepository.existsActiveDangKyThangByBienSoXe(createDTO.getBienSoXe(), now)) {
            throw new IdInvalidException(
                    "Biển số xe '" + createDTO.getBienSoXe() + "' đã có đăng ký tháng còn hiệu lực");
        }

        // 3. Kiểm tra nhân viên có tồn tại và có quyền tạo đăng ký không
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
            vehicle.setSoCavet(createDTO.getSoCavet());
            vehicle.setMaLoaiXe(vehicleType);
            vehicle.setOwner(user);
            vehicle = vehicleService.handleCreateVehicle(vehicle);
        } else {
            // Cập nhật owner và soCavet cho Vehicle hiện có nếu cần
            boolean needUpdate = false;
            if (vehicle.getOwner() == null ||
                    vehicle.getOwner().getId() != user.getId()) {
                vehicle.setOwner(user);
                needUpdate = true;
            }
            if (vehicle.getSoCavet() == null || !vehicle.getSoCavet().equals(createDTO.getSoCavet())) {
                vehicle.setSoCavet(createDTO.getSoCavet());
                needUpdate = true;
            }
            if (needUpdate) {
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
        if (id == null || id <= 0) {
            return null;
        }
        Optional<DangKyThang> dangKyThangOptional = dangKyThangRepository.findById(id);
        return dangKyThangOptional.orElse(null);
    }

    public List<DangKyThang> getDangKyThangByBienSoXe(String bienSoXe) {
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) {
            throw new IllegalArgumentException("Biển số xe không được để trống");
        }
        return dangKyThangRepository.findByBienSoXeOrderByCreatedDateDesc(bienSoXe);
    }

    public List<DangKyThang> getDangKyThangByCccd(String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) {
            throw new IllegalArgumentException("CCCD không được để trống");
        }
        if (!cccd.matches("^[0-9]{12}$")) {
            throw new IllegalArgumentException("CCCD phải có đúng 12 chữ số");
        }
        return dangKyThangRepository.findByCccdOrderByCreatedDateDesc(cccd);
    }

    public List<DangKyThang> getDangKyThangByNhanVien(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên không được để trống");
        }
        return dangKyThangRepository.findByNhanVienTaoMaNVOrderByCreatedDateDesc(maNV);
    }

    @Transactional
    public DangKyThang handleUpdateDangKyThang(Long id, DangKyThangUpdateDTO updateDTO) throws IdInvalidException {
        // 1. Validation cơ bản
        if (id == null || id <= 0) {
            throw new IdInvalidException("ID đăng ký tháng không hợp lệ");
        }

        if (updateDTO == null) {
            throw new IdInvalidException("Dữ liệu cập nhật không được để trống");
        }

        DangKyThang existingDangKyThang = fetchDangKyThangById(id);
        if (existingDangKyThang == null) {
            throw new IdInvalidException("Đăng ký tháng với ID " + id + " không tồn tại");
        }

        // 2. Kiểm tra trạng thái đăng ký
        if (existingDangKyThang.isExpired()) {
            throw new IdInvalidException("Không thể cập nhật đăng ký tháng đã hết hạn");
        }

        if (existingDangKyThang.getTrangThai() == DangKyThang.TrangThaiDangKy.CANCELLED) {
            throw new IdInvalidException("Không thể cập nhật đăng ký tháng đã bị hủy");
        }

        // 3. Kiểm tra nhân viên có quyền cập nhật không
        Staff staff = staffService.fetchStaffByMaNV(updateDTO.getMaNhanVien());
        if (staff == null || (!staff.isAdmin() && !staff.isBaoVe())) {
            throw new IdInvalidException("Nhân viên không có quyền cập nhật đăng ký tháng");
        }

        // 4. Kiểm tra logic giảm số tháng - chỉ cho phép nếu chưa vượt quá thời điểm
        // mới
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = existingDangKyThang.getThoiGianBatDau();

        // Tính thời gian hết hạn mới dựa trên số tháng giảm
        LocalDateTime newEndTime = startTime.plusMonths(updateDTO.getSoThang());

        // Kiểm tra: thời điểm hết hạn mới phải sau thời điểm hiện tại
        if (newEndTime.isBefore(now)) {
            throw new IdInvalidException(
                    String.format("Không thể giảm xuống %d tháng vì thời gian này đã qua. " +
                            "Thời gian hết hạn mới sẽ là %s, nhưng hiện tại đã là %s",
                            updateDTO.getSoThang(),
                            newEndTime.toLocalDate(),
                            now.toLocalDate()));
        }

        // Kiểm tra số tháng mới phải nhỏ hơn số tháng hiện tại (chỉ cho phép giảm)
        if (updateDTO.getSoThang() >= existingDangKyThang.getSoThang()) {
            throw new IdInvalidException(
                    String.format("Chỉ có thể giảm số tháng đăng ký. " +
                            "Số tháng hiện tại: %d, số tháng mới: %d. " +
                            "Để tăng thời gian, vui lòng sử dụng chức năng gia hạn",
                            existingDangKyThang.getSoThang(), updateDTO.getSoThang()));
        }

        // 5. Cập nhật số tháng và tính lại tiền
        // Log để debug
        System.out.println("=== DEBUG UPDATE ===");
        System.out.println("Số tháng cũ: " + existingDangKyThang.getSoThang());
        System.out.println("Số tháng mới: " + updateDTO.getSoThang());
        System.out.println("Thời gian bắt đầu: " + existingDangKyThang.getThoiGianBatDau());
        System.out.println("Thời gian hết hạn cũ: " + existingDangKyThang.getThoiGianHetHan());
        System.out.println("Thời gian hết hạn mới: " + newEndTime);
        System.out.println("===================");

        existingDangKyThang.setSoThang(updateDTO.getSoThang());
        existingDangKyThang.setThoiGianHetHan(newEndTime);

        // Tính lại số tiền thanh toán
        BigDecimal newAmount = priceService.calculateMonthlyPrice(
                existingDangKyThang.getLoaiXe().getMaLoaiXe(),
                updateDTO.getSoThang());
        existingDangKyThang.setSoTienThanhToan(newAmount);

        // 6. Cập nhật thời gian sửa đổi
        existingDangKyThang.setUpdatedDate(LocalDateTime.now());

        return dangKyThangRepository.save(existingDangKyThang);
    }

    @Transactional
    public DangKyThang handleExtendDangKyThang(Long id, Integer soThangMoi, String maNhanVien)
            throws IdInvalidException {
        // 1. Validation cơ bản
        if (id == null || id <= 0) {
            throw new IdInvalidException("ID đăng ký tháng không hợp lệ");
        }

        if (soThangMoi == null || soThangMoi < 1 || soThangMoi > 12) {
            throw new IdInvalidException("Số tháng gia hạn phải từ 1 đến 12");
        }

        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IdInvalidException("Mã nhân viên không được để trống");
        }

        DangKyThang existingDangKyThang = fetchDangKyThangById(id);
        if (existingDangKyThang == null) {
            throw new IdInvalidException("Đăng ký tháng với ID " + id + " không tồn tại");
        }

        // 2. Kiểm tra trạng thái đăng ký
        if (existingDangKyThang.getTrangThai() == DangKyThang.TrangThaiDangKy.CANCELLED) {
            throw new IdInvalidException("Không thể gia hạn đăng ký tháng đã bị hủy");
        }

        // 3. Kiểm tra nhân viên có quyền gia hạn không
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null || (!staff.isAdmin() && !staff.isBaoVe())) {
            throw new IdInvalidException("Nhân viên không có quyền gia hạn đăng ký tháng");
        }

        // 4. Kiểm tra tổng số tháng sau gia hạn không vượt quá giới hạn
        int totalMonths = existingDangKyThang.getSoThang() + soThangMoi;
        if (totalMonths > 24) {
            throw new IdInvalidException("Tổng số tháng đăng ký không được vượt quá 24 tháng (hiện tại: "
                    + existingDangKyThang.getSoThang() + " + gia hạn: " + soThangMoi + " = " + totalMonths + ")");
        }

        // Gia hạn từ thời điểm hết hạn hiện tại
        LocalDateTime newThoiGianHetHan = existingDangKyThang.getThoiGianHetHan().plusMonths(soThangMoi);
        existingDangKyThang.setThoiGianHetHan(newThoiGianHetHan);
        existingDangKyThang.setSoThang(existingDangKyThang.getSoThang() + soThangMoi);

        // Tính tiền gia hạn và cộng vào tổng tiền
        BigDecimal giaGiaHan = priceService.calculateMonthlyPrice(
                existingDangKyThang.getLoaiXe().getMaLoaiXe(),
                soThangMoi);
        existingDangKyThang.setSoTienThanhToan(
                existingDangKyThang.getSoTienThanhToan().add(giaGiaHan));

        // Nếu đăng ký đã hết hạn, kích hoạt lại
        if (existingDangKyThang.isExpired()) {
            existingDangKyThang.setTrangThai(DangKyThang.TrangThaiDangKy.ACTIVE);
        }

        return dangKyThangRepository.save(existingDangKyThang);
    }

    @Transactional
    public void handleCancelDangKyThang(Long id, String maNhanVien) throws IdInvalidException {
        // 1. Validation cơ bản
        if (id == null || id <= 0) {
            throw new IdInvalidException("ID đăng ký tháng không hợp lệ");
        }

        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IdInvalidException("Mã nhân viên không được để trống");
        }

        DangKyThang existingDangKyThang = fetchDangKyThangById(id);
        if (existingDangKyThang == null) {
            throw new IdInvalidException("Đăng ký tháng với ID " + id + " không tồn tại");
        }

        // 2. Kiểm tra trạng thái đăng ký
        if (existingDangKyThang.getTrangThai() == DangKyThang.TrangThaiDangKy.CANCELLED) {
            throw new IdInvalidException("Đăng ký tháng này đã bị hủy trước đó");
        }

        if (existingDangKyThang.getTrangThai() == DangKyThang.TrangThaiDangKy.EXPIRED) {
            throw new IdInvalidException("Không thể hủy đăng ký tháng đã hết hạn");
        }

        // 3. Kiểm tra nhân viên có quyền hủy không (chỉ Admin)
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null || !staff.isAdmin()) {
            throw new IdInvalidException("Chỉ Admin mới có quyền hủy đăng ký tháng");
        }

        // 4. Kiểm tra nghiệp vụ: có thể thêm logic kiểm tra thời gian hủy
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime registrationStart = existingDangKyThang.getThoiGianBatDau();

        // Không cho phép hủy nếu đã đăng ký quá 7 ngày (business rule)
        if (registrationStart.isBefore(now.minusDays(7))) {
            throw new IdInvalidException("Không thể hủy đăng ký tháng đã được tạo quá 7 ngày");
        }

        existingDangKyThang.cancel();
        dangKyThangRepository.save(existingDangKyThang);
    }

    @Transactional
    public DangKyThang handleRenewDangKyThang(String bienSoXe, Integer soThang, String maNhanVien)
            throws IdInvalidException {
        // 1. Validation cơ bản
        if (bienSoXe == null || bienSoXe.trim().isEmpty()) {
            throw new IdInvalidException("Biển số xe không được để trống");
        }

        if (soThang == null || soThang < 1 || soThang > 12) {
            throw new IdInvalidException("Số tháng đăng ký phải từ 1 đến 12");
        }

        if (maNhanVien == null || maNhanVien.trim().isEmpty()) {
            throw new IdInvalidException("Mã nhân viên không được để trống");
        }

        // 2. Kiểm tra nhân viên có quyền tạo đăng ký không
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IdInvalidException("Nhân viên với mã " + maNhanVien + " không tồn tại");
        }

        if (!staff.isAdmin() && !staff.isBaoVe()) {
            throw new IdInvalidException("Nhân viên không có quyền tạo đăng ký tháng");
        }

        // 3. Kiểm tra xe có tồn tại không
        Vehicle vehicle = vehicleService.fetchVehicleByBienSoXe(bienSoXe);
        if (vehicle == null) {
            throw new IdInvalidException("Xe với biển số " + bienSoXe + " không tồn tại");
        }

        // 4. Kiểm tra xe đã có đăng ký tháng còn hiệu lực chưa
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

        // Tính số tiền thanh toán dựa trên Price, ParkingMode, VehicleType và số tháng
        BigDecimal soTienThanhToan = priceService.calculateMonthlyPrice(
                vehicle.getMaLoaiXe().getMaLoaiXe(),
                soThang);
        dangKyThang.setSoTienThanhToan(soTienThanhToan);

        return dangKyThangRepository.save(dangKyThang);
    }

    public List<DangKyThang> getDangKyThangByTrangThai(DangKyThang.TrangThaiDangKy trangThai) {
        return dangKyThangRepository.findByTrangThaiOrderByCreatedDateDesc(trangThai);
    }

    public List<DangKyThang> getDangKyThangInDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }
        // Giới hạn khoảng thời gian tối đa 1 năm để tránh query quá lớn
        if (startDate.isBefore(endDate.minusYears(1))) {
            throw new IllegalArgumentException("Khoảng thời gian tìm kiếm không được vượt quá 1 năm");
        }
        return dangKyThangRepository.findByThoiGianBatDauBetween(startDate, endDate);
    }

    // ============== VALIDATION & BUSINESS LOGIC METHODS ==============

    /**
     * Kiểm tra xe có đăng ký tháng còn hiệu lực không
     */
    public boolean hasActiveDangKyThang(String bienSoXe) {
        return dangKyThangRepository.existsActiveDangKyThangByBienSoXe(bienSoXe, LocalDateTime.now());
    }

    /**
     * Lấy đăng ký tháng còn hiệu lực của xe
     */
    public DangKyThang getActiveDangKyThang(String bienSoXe) {
        return dangKyThangRepository.findActiveDangKyThangByBienSoXe(bienSoXe, LocalDateTime.now()).orElse(null);
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
