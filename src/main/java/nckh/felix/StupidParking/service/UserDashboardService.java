package nckh.felix.StupidParking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.DangKyThang;
import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.User;
import nckh.felix.StupidParking.domain.Vehicle;
import nckh.felix.StupidParking.domain.dto.UserDashboardDTO;
import nckh.felix.StupidParking.repository.DangKyThangRepository;
import nckh.felix.StupidParking.repository.StaffRepository;
import nckh.felix.StupidParking.repository.VehicleRepository;

@Service
public class UserDashboardService {

    private final UserService userService;
    private final VehicleRepository vehicleRepository;
    private final DangKyThangRepository dangKyThangRepository;
    private final DangKyThangService dangKyThangService;
    private final PriceService priceService;
    private final StaffRepository staffRepository;

    public UserDashboardService(UserService userService, VehicleRepository vehicleRepository,
            DangKyThangRepository dangKyThangRepository, DangKyThangService dangKyThangService,
            PriceService priceService, StaffRepository staffRepository) {
        this.userService = userService;
        this.vehicleRepository = vehicleRepository;
        this.dangKyThangRepository = dangKyThangRepository;
        this.dangKyThangService = dangKyThangService;
        this.priceService = priceService;
        this.staffRepository = staffRepository;
    }

    /**
     * Lấy toàn bộ thông tin dashboard cho User
     * 
     * @param email Email của User
     * @return UserDashboardDTO chứa đầy đủ thông tin
     */
    public UserDashboardDTO getUserDashboard(String email) {
        // 1. Lấy thông tin User
        User user = userService.fetchUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User với email " + email + " không tồn tại");
        }

        // 2. Lấy thông tin User
        UserDashboardDTO.UserInfo userInfo = new UserDashboardDTO.UserInfo(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getCccd(),
                user.getSdt(),
                user.getDiaChi(),
                user.getCreatedDate());

        // 3. Lấy danh sách xe của User
        List<Vehicle> userVehicles = vehicleRepository.findByOwnerEmail(email);
        List<UserDashboardDTO.VehicleInfo> vehicleInfos = new ArrayList<>();

        for (Vehicle vehicle : userVehicles) {
            // Kiểm tra xe có đăng ký tháng active không
            DangKyThang activeDangKy = dangKyThangService.getActiveDangKyThang(vehicle.getBienSoXe());
            boolean hasActiveDangKy = activeDangKy != null;
            LocalDateTime dangKyExpiry = (hasActiveDangKy && activeDangKy != null) ? activeDangKy.getThoiGianHetHan()
                    : null;

            UserDashboardDTO.VehicleInfo vehicleInfo = new UserDashboardDTO.VehicleInfo(
                    vehicle.getBienSoXe(),
                    vehicle.getTenXe(),
                    vehicle.getMaLoaiXe().getMaLoaiXe(),
                    vehicle.getMaLoaiXe().getTenLoaiXe(),
                    vehicle.getCreatedDate(),
                    hasActiveDangKy,
                    dangKyExpiry);
            vehicleInfos.add(vehicleInfo);
        }

        // 4. Lấy lịch sử đăng ký tháng của User theo CCCD
        List<DangKyThang> userDangKyThangs = dangKyThangRepository.findByCccdOrderByCreatedDateDesc(user.getCccd());
        List<UserDashboardDTO.DangKyThangInfo> dangKyThangInfos = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        for (DangKyThang dangKy : userDangKyThangs) {
            boolean isActive = dangKy.getTrangThai() == DangKyThang.TrangThaiDangKy.ACTIVE
                    && dangKy.getThoiGianHetHan().isAfter(now);
            boolean isExpired = dangKy.getThoiGianHetHan().isBefore(now)
                    || dangKy.getTrangThai() == DangKyThang.TrangThaiDangKy.EXPIRED;

            long daysUntilExpiry = isActive ? ChronoUnit.DAYS.between(now, dangKy.getThoiGianHetHan()) : 0;

            // Lấy tên xe từ Vehicle repository để tránh lazy loading
            String tenXe = "Không xác định";
            try {
                Vehicle vehicle = vehicleRepository.findById(dangKy.getBienSoXe()).orElse(null);
                if (vehicle != null) {
                    tenXe = vehicle.getTenXe();
                }
            } catch (Exception e) {
                // Nếu không tìm thấy Vehicle, giữ giá trị mặc định
                tenXe = "Xe không tồn tại";
            }

            UserDashboardDTO.DangKyThangInfo dangKyInfo = new UserDashboardDTO.DangKyThangInfo(
                    dangKy.getId(),
                    dangKy.getBienSoXe(),
                    tenXe,
                    dangKy.getSoThang(),
                    dangKy.getSoTienThanhToan(),
                    dangKy.getThoiGianBatDau(),
                    dangKy.getThoiGianHetHan(),
                    dangKy.getTrangThai().getDescription(),
                    dangKy.getGhiChu(),
                    isActive,
                    isExpired,
                    daysUntilExpiry);
            dangKyThangInfos.add(dangKyInfo);
        }

        // 5. Tính toán thống kê tổng quan
        UserDashboardDTO.DashboardSummary summary = calculateDashboardSummary(
                vehicleInfos, dangKyThangInfos, userDangKyThangs);

        // 6. Tạo và trả về UserDashboardDTO
        return new UserDashboardDTO(userInfo, vehicleInfos, dangKyThangInfos, summary);
    }

    /**
     * Tính toán thống kê tổng quan cho dashboard
     */
    private UserDashboardDTO.DashboardSummary calculateDashboardSummary(
            List<UserDashboardDTO.VehicleInfo> vehicles,
            List<UserDashboardDTO.DangKyThangInfo> dangKyThangs,
            List<DangKyThang> allDangKyThangs) {

        int totalVehicles = vehicles.size();
        int activeRegistrations = (int) dangKyThangs.stream().filter(dk -> dk.isActive()).count();
        int expiredRegistrations = (int) dangKyThangs.stream().filter(dk -> dk.isExpired()).count();

        // Tính tổng số tiền đã thanh toán
        BigDecimal totalAmountPaid = allDangKyThangs.stream()
                .map(DangKyThang::getSoTienThanhToan)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Tìm đăng ký sắp hết hạn nhất
        LocalDateTime nextExpiryDate = null;
        String nextExpiryVehicle = null;

        for (UserDashboardDTO.DangKyThangInfo dangKy : dangKyThangs) {
            if (dangKy.isActive()) {
                if (nextExpiryDate == null || dangKy.getThoiGianHetHan().isBefore(nextExpiryDate)) {
                    nextExpiryDate = dangKy.getThoiGianHetHan();
                    nextExpiryVehicle = dangKy.getBienSoXe();
                }
            }
        }

        return new UserDashboardDTO.DashboardSummary(
                totalVehicles,
                activeRegistrations,
                expiredRegistrations,
                totalAmountPaid,
                nextExpiryDate,
                nextExpiryVehicle);
    }

    /**
     * Lấy danh sách xe của User theo email
     */
    public List<UserDashboardDTO.VehicleInfo> getUserVehicles(String email) {
        List<Vehicle> vehicles = vehicleRepository.findByOwnerEmail(email);
        List<UserDashboardDTO.VehicleInfo> vehicleInfos = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            DangKyThang activeDangKy = dangKyThangService.getActiveDangKyThang(vehicle.getBienSoXe());
            boolean hasActiveDangKy = activeDangKy != null;
            LocalDateTime dangKyExpiry = hasActiveDangKy ? activeDangKy.getThoiGianHetHan() : null;

            UserDashboardDTO.VehicleInfo vehicleInfo = new UserDashboardDTO.VehicleInfo(
                    vehicle.getBienSoXe(),
                    vehicle.getTenXe(),
                    vehicle.getMaLoaiXe().getMaLoaiXe(),
                    vehicle.getMaLoaiXe().getTenLoaiXe(),
                    vehicle.getCreatedDate(),
                    hasActiveDangKy,
                    dangKyExpiry);
            vehicleInfos.add(vehicleInfo);
        }

        return vehicleInfos;
    }

    /**
     * Lấy lịch sử đăng ký tháng của User theo email
     */
    public List<UserDashboardDTO.DangKyThangInfo> getUserDangKyThangHistory(String email) {
        User user = userService.fetchUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User với email " + email + " không tồn tại");
        }

        List<DangKyThang> dangKyThangs = dangKyThangRepository.findByCccdOrderByCreatedDateDesc(user.getCccd());
        List<UserDashboardDTO.DangKyThangInfo> dangKyThangInfos = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        for (DangKyThang dangKy : dangKyThangs) {
            boolean isActive = dangKy.getTrangThai() == DangKyThang.TrangThaiDangKy.ACTIVE
                    && dangKy.getThoiGianHetHan().isAfter(now);
            boolean isExpired = dangKy.getThoiGianHetHan().isBefore(now)
                    || dangKy.getTrangThai() == DangKyThang.TrangThaiDangKy.EXPIRED;

            long daysUntilExpiry = isActive ? ChronoUnit.DAYS.between(now, dangKy.getThoiGianHetHan()) : 0;

            String tenXe = dangKy.getVehicle() != null ? dangKy.getVehicle().getTenXe() : "Không xác định";

            UserDashboardDTO.DangKyThangInfo dangKyInfo = new UserDashboardDTO.DangKyThangInfo(
                    dangKy.getId(),
                    dangKy.getBienSoXe(),
                    tenXe,
                    dangKy.getSoThang(),
                    dangKy.getSoTienThanhToan(),
                    dangKy.getThoiGianBatDau(),
                    dangKy.getThoiGianHetHan(),
                    dangKy.getTrangThai().getDescription(),
                    dangKy.getGhiChu(),
                    isActive,
                    isExpired,
                    daysUntilExpiry);
            dangKyThangInfos.add(dangKyInfo);
        }

        return dangKyThangInfos;
    }

    /**
     * Yêu cầu gia hạn đăng ký tháng từ User
     * Tạo một record mới với trạng thái PENDING để chờ Staff xác nhận
     * 
     * @param email         Email của User
     * @param dangKyThangId ID của đăng ký tháng hiện tại
     * @param soThangGiaHan Số tháng muốn gia hạn
     * @param ghiChu        Ghi chú từ User
     * @return DangKyThang mới với trạng thái PENDING
     */
    public DangKyThang requestExtension(String email, Long dangKyThangId, Integer soThangGiaHan, String ghiChu) {
        // 1. Kiểm tra User có tồn tại không
        User user = userService.fetchUserByEmail(email);
        if (user == null) {
            throw new IllegalArgumentException("User với email " + email + " không tồn tại");
        }

        // 2. Lấy đăng ký tháng hiện tại
        DangKyThang currentDangKy = dangKyThangRepository.findById(dangKyThangId)
                .orElseThrow(
                        () -> new IllegalArgumentException("Đăng ký tháng với ID " + dangKyThangId + " không tồn tại"));

        // 3. Kiểm tra quyền sở hữu (CCCD của đăng ký phải khớp với CCCD của User)
        if (!currentDangKy.getCccd().equals(user.getCccd())) {
            throw new IllegalArgumentException("User không có quyền gia hạn đăng ký tháng này");
        }

        // 4. Kiểm tra trạng thái đăng ký hiện tại
        if (currentDangKy.getTrangThai() != DangKyThang.TrangThaiDangKy.ACTIVE) {
            throw new IllegalArgumentException("Chỉ có thể gia hạn cho đăng ký tháng đang ACTIVE");
        }

        // 5. Kiểm tra xem có yêu cầu gia hạn PENDING nào cho đăng ký này không
        boolean hasPendingExtension = dangKyThangRepository.existsByParentIdAndTrangThai(
                dangKyThangId, DangKyThang.TrangThaiDangKy.PENDING);
        if (hasPendingExtension) {
            throw new IllegalArgumentException("Đã có yêu cầu gia hạn đang chờ xử lý cho đăng ký này");
        }

        // 6. Tính toán thời gian và tiền
        LocalDateTime newStartTime = currentDangKy.getThoiGianHetHan();
        LocalDateTime newEndTime = newStartTime.plusMonths(soThangGiaHan);

        // Lấy giá từ PriceService (tính dựa trên loại xe và số tháng)
        BigDecimal extensionPrice = priceService.calculateMonthlyPrice(
                currentDangKy.getLoaiXe().getMaLoaiXe(), soThangGiaHan);

        // 7. Tạo đăng ký gia hạn mới với trạng thái PENDING
        DangKyThang extensionRequest = new DangKyThang();
        extensionRequest.setBienSoXe(currentDangKy.getBienSoXe());
        extensionRequest.setCccd(currentDangKy.getCccd());
        extensionRequest.setSoCavet(currentDangKy.getSoCavet());
        extensionRequest.setDiaChi(currentDangKy.getDiaChi());
        extensionRequest.setLoaiXe(currentDangKy.getLoaiXe());
        extensionRequest.setSoThang(soThangGiaHan);
        extensionRequest.setThoiGianBatDau(newStartTime);
        extensionRequest.setThoiGianHetHan(newEndTime);
        extensionRequest.setSoTienThanhToan(extensionPrice);
        extensionRequest.setTrangThai(DangKyThang.TrangThaiDangKy.PENDING);
        extensionRequest.setTrangThaiThanhToan(DangKyThang.TrangThaiThanhToan.PENDING);
        extensionRequest.setParentId(dangKyThangId); // Liên kết với đăng ký gốc
        extensionRequest.setLanGiaHan(currentDangKy.getLanGiaHan() + 1);
        extensionRequest.setGhiChu("YÊU CẦU GIA HẠN TỪ USER: " + (ghiChu != null ? ghiChu : ""));

        // Tạm thời sử dụng Staff đầu tiên trong hệ thống cho yêu cầu gia hạn
        // Logic này sẽ được điều chỉnh khi có Staff "SYSTEM" hoặc thay đổi schema
        List<Staff> allStaff = staffRepository.findAll();
        if (allStaff.isEmpty()) {
            throw new RuntimeException("Không tìm thấy Staff nào trong hệ thống");
        }
        extensionRequest.setNhanVienTao(allStaff.get(0));

        return dangKyThangRepository.save(extensionRequest);
    }
}
