package nckh.felix.StupidParking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import nckh.felix.StupidParking.domain.ParkingTransaction;
import nckh.felix.StupidParking.domain.ParkingTransaction.TrangThaiGiaoDich;
import nckh.felix.StupidParking.domain.ParkingLot;
import nckh.felix.StupidParking.domain.Staff;
import nckh.felix.StupidParking.domain.Vehicle;
import nckh.felix.StupidParking.domain.VehicleType;
import nckh.felix.StupidParking.repository.ParkingTransactionRepository;
import nckh.felix.StupidParking.util.error.IdInvalidException;

@Service
@Transactional
public class ParkingTransactionService {

    private final ParkingTransactionRepository parkingTransactionRepository;
    private final ParkingLotService parkingLotService;
    private final StaffService staffService;
    private final PriceService priceService;
    private final VehicleService vehicleService;
    private final VehicleTypeService vehicleTypeService;

    public ParkingTransactionService(ParkingTransactionRepository parkingTransactionRepository,
            ParkingLotService parkingLotService,
            StaffService staffService,
            PriceService priceService,
            VehicleService vehicleService,
            VehicleTypeService vehicleTypeService) {
        this.parkingTransactionRepository = parkingTransactionRepository;
        this.parkingLotService = parkingLotService;
        this.staffService = staffService;
        this.priceService = priceService;
        this.vehicleService = vehicleService;
        this.vehicleTypeService = vehicleTypeService;
    }

    /**
     * Tạo yêu cầu xe vào bãi đỗ
     * Tự động tạo Vehicle mới nếu xe chưa tồn tại trong database
     */
    public ParkingTransaction createEntryRequest(String bienSoXe, String maBaiDo, String maLoaiXe, String ghiChu) {
        // Kiểm tra xe có đang đỗ trong bãi không
        if (parkingTransactionRepository.countVehicleCurrentlyParked(bienSoXe) > 0) {
            throw new IllegalStateException("Xe " + bienSoXe + " đang đỗ trong bãi");
        }

        // Kiểm tra bãi đỗ
        ParkingLot parkingLot = parkingLotService.fetchParkingLotByMaBaiDo(maBaiDo);
        if (parkingLot == null) {
            throw new IllegalArgumentException("Không tìm thấy bãi đỗ: " + maBaiDo);
        }

        // KIỂM TRA LOẠI XE CÓ PHÙ HỢP VỚI BÃI ĐỖ KHÔNG
        if (!parkingLot.getMaLoaiXe().getMaLoaiXe().equals(maLoaiXe)) {
            throw new IllegalArgumentException("Loại xe " + maLoaiXe + " không phù hợp với bãi đỗ " + maBaiDo
                    + " (chỉ dành cho " + parkingLot.getMaLoaiXe().getMaLoaiXe() + ")");
        }

        if (!parkingLot.canPark()) {
            throw new IllegalStateException("Bãi đỗ không thể nhận thêm xe");
        }

        // Kiểm tra và tạo Vehicle mới nếu chưa tồn tại
        Vehicle vehicle = vehicleService.fetchVehicleByBienSoXe(bienSoXe);
        if (vehicle == null) {
            // KIỂM TRA LOẠI XE CÓ TỒN TẠI KHÔNG
            VehicleType vehicleType = vehicleTypeService.fetchVehicleTypeByMaLoaiXe(maLoaiXe);
            if (vehicleType == null) {
                throw new IllegalArgumentException("Không tìm thấy loại xe: " + maLoaiXe);
            }

            // Tạo xe mới với thông tin cơ bản
            vehicle = new Vehicle();
            vehicle.setBienSoXe(bienSoXe);
            vehicle.setMaLoaiXe(vehicleType);
            vehicle.setTenXe("Xe " + maLoaiXe + " - " + bienSoXe); // Tên mặc định
            vehicle = vehicleService.handleCreateVehicle(vehicle);
        } else {
            // KIỂM TRA LOẠI XE CỦA XE ĐÃ TỒN TẠI CÓ KHỚP KHÔNG
            if (!vehicle.getMaLoaiXe().getMaLoaiXe().equals(maLoaiXe)) {
                throw new IllegalArgumentException("Xe " + bienSoXe + " là loại " + vehicle.getMaLoaiXe().getMaLoaiXe()
                        + ", không phải " + maLoaiXe);
            }
        }

        // Tạo giao dịch mới
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setBienSoXe(bienSoXe);
        transaction.setParkingLot(parkingLot);
        transaction.setVehicleType(new VehicleType(maLoaiXe));
        transaction.setThoiGianVao(LocalDateTime.now());
        transaction.setTrangThai(TrangThaiGiaoDich.PENDING_IN);
        transaction.setGhiChu(ghiChu);

        return parkingTransactionRepository.save(transaction);
    }

    /**
     * CHO XE VÀO TRỰC TIẾP - DÀNH CHO MOBILE/CAMERA SCAN
     * Kết hợp tạo yêu cầu và duyệt vào trong 1 bước
     */
    public ParkingTransaction directVehicleEntry(String bienSoXe, String maBaiDo, String maLoaiXe, String maNhanVien,
            String ghiChu) throws IdInvalidException {
        // Kiểm tra xe có đang đỗ trong bãi không
        if (parkingTransactionRepository.countVehicleCurrentlyParked(bienSoXe) > 0) {
            throw new IllegalStateException("Xe " + bienSoXe + " đang đỗ trong bãi");
        }

        // Kiểm tra nhân viên
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên: " + maNhanVien);
        }

        // Kiểm tra bãi đỗ
        ParkingLot parkingLot = parkingLotService.fetchParkingLotByMaBaiDo(maBaiDo);
        if (parkingLot == null) {
            throw new IllegalArgumentException("Không tìm thấy bãi đỗ: " + maBaiDo);
        }

        // KIỂM TRA LOẠI XE CÓ PHÙ HỢP VỚI BÃI ĐỖ KHÔNG
        if (!parkingLot.getMaLoaiXe().getMaLoaiXe().equals(maLoaiXe)) {
            throw new IllegalArgumentException("Loại xe " + maLoaiXe + " không phù hợp với bãi đỗ " + maBaiDo
                    + " (chỉ dành cho " + parkingLot.getMaLoaiXe().getMaLoaiXe() + ")");
        }

        if (!parkingLot.canPark()) {
            throw new IllegalStateException("Bãi đỗ không thể nhận thêm xe");
        }

        // Kiểm tra và tạo Vehicle mới nếu chưa tồn tại
        Vehicle vehicle = vehicleService.fetchVehicleByBienSoXe(bienSoXe);
        if (vehicle == null) {
            // KIỂM TRA LOẠI XE CÓ TỒN TẠI KHÔNG
            VehicleType vehicleType = vehicleTypeService.fetchVehicleTypeByMaLoaiXe(maLoaiXe);
            if (vehicleType == null) {
                throw new IllegalArgumentException("Không tìm thấy loại xe: " + maLoaiXe);
            }

            // Tạo xe mới với thông tin cơ bản
            vehicle = new Vehicle();
            vehicle.setBienSoXe(bienSoXe);
            vehicle.setMaLoaiXe(vehicleType);
            vehicle.setTenXe("Xe " + maLoaiXe + " - " + bienSoXe); // Tên mặc định
            vehicle = vehicleService.handleCreateVehicle(vehicle);
        } else {
            // KIỂM TRA LOẠI XE CỦA XE ĐÃ TỒN TẠI CÓ KHỚP KHÔNG
            if (!vehicle.getMaLoaiXe().getMaLoaiXe().equals(maLoaiXe)) {
                throw new IllegalArgumentException("Xe " + bienSoXe + " là loại " + vehicle.getMaLoaiXe().getMaLoaiXe()
                        + ", không phải " + maLoaiXe);
            }
        }

        // Tạo giao dịch và CHO VÀO TRỰC TIẾP
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setBienSoXe(bienSoXe);
        transaction.setParkingLot(parkingLot);
        transaction.setVehicleType(vehicle.getMaLoaiXe());
        transaction.setThoiGianVao(LocalDateTime.now());
        transaction.setGhiChu(ghiChu);

        // DUYỆT VÀO NGAY LẬP TỨC
        transaction.approveEntry(staff);

        // Lưu giao dịch trước
        transaction = parkingTransactionRepository.save(transaction);

        // Cập nhật số lượng xe trong bãi đỗ
        parkingLotService.handleParkVehicle(parkingLot.getMaBaiDo());

        return transaction;
    }

    /**
     * Duyệt xe vào bãi đỗ
     */
    public ParkingTransaction approveEntry(Long maGiaoDich, String maNhanVien) throws IdInvalidException {
        ParkingTransaction transaction = fetchTransactionById(maGiaoDich);

        if (transaction.getTrangThai() != TrangThaiGiaoDich.PENDING_IN) {
            throw new IllegalStateException("Giao dịch không ở trạng thái chờ duyệt vào");
        }

        // KIỂM TRA XE CÓ ĐANG ĐỖ TRONG BÃI KHÔNG (FIX LỖI)
        if (parkingTransactionRepository.countVehicleCurrentlyParked(transaction.getBienSoXe()) > 0) {
            throw new IllegalStateException(
                    "Xe " + transaction.getBienSoXe() + " đang đỗ trong bãi, không thể duyệt vào thêm");
        }

        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên: " + maNhanVien);
        }

        // Kiểm tra lại bãi đỗ có chỗ không
        if (!transaction.getParkingLot().canPark()) {
            throw new IllegalStateException("Bãi đỗ đã hết chỗ");
        }

        // Duyệt xe vào
        transaction.approveEntry(staff);

        // Cập nhật số lượng xe trong bãi đỗ
        parkingLotService.handleParkVehicle(transaction.getParkingLot().getMaBaiDo());

        return parkingTransactionRepository.save(transaction);
    }

    /**
     * Tạo yêu cầu xe ra
     */
    public ParkingTransaction createExitRequest(String bienSoXe) {
        Optional<ParkingTransaction> activeTransaction = parkingTransactionRepository
                .findActiveTransactionByBienSoXe(bienSoXe);

        if (activeTransaction.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy xe " + bienSoXe + " trong bãi đỗ");
        }

        ParkingTransaction transaction = activeTransaction.get();
        transaction.requestExit();

        return parkingTransactionRepository.save(transaction);
    }

    /**
     * Duyệt xe ra và thanh toán
     */
    public ParkingTransaction approveExit(Long maGiaoDich, String maNhanVien, BigDecimal soTienThanhToan)
            throws IdInvalidException {
        ParkingTransaction transaction = fetchTransactionById(maGiaoDich);

        if (transaction.getTrangThai() != TrangThaiGiaoDich.PENDING_OUT) {
            throw new IllegalStateException("Giao dịch không ở trạng thái chờ duyệt ra");
        }

        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên: " + maNhanVien);
        }

        // Hoàn thành giao dịch
        transaction.completeTransaction(staff, soTienThanhToan);

        // Cập nhật số lượng xe trong bãi đỗ
        parkingLotService.handleUnparkVehicle(transaction.getParkingLot().getMaBaiDo());

        return parkingTransactionRepository.save(transaction);
    }

    /**
     * Tính tiền đỗ xe tự động
     */
    public BigDecimal calculateParkingFee(Long maGiaoDich) throws IdInvalidException {
        ParkingTransaction transaction = fetchTransactionById(maGiaoDich);

        long hours = transaction.getParkingDurationInHours();
        if (hours < 1)
            hours = 1; // Tối thiểu 1 giờ

        // Lấy giá từ bảng Price theo loại xe
        BigDecimal hourlyRate = priceService.getHourlyRateByVehicleType(
                transaction.getVehicleType().getMaLoaiXe());

        BigDecimal calculatedFee = hourlyRate.multiply(BigDecimal.valueOf(hours));

        // CẬP NHẬT SỐ TIỀN VÀO GIAO DỊCH
        transaction.setSoTien(calculatedFee);
        parkingTransactionRepository.save(transaction);

        return calculatedFee;
    }

    /**
     * Hủy giao dịch
     */
    public ParkingTransaction cancelTransaction(Long maGiaoDich, String maNhanVien) throws IdInvalidException {
        ParkingTransaction transaction = fetchTransactionById(maGiaoDich);

        if (transaction.getTrangThai() == TrangThaiGiaoDich.COMPLETED) {
            throw new IllegalStateException("Không thể hủy giao dịch đã hoàn thành");
        }

        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên: " + maNhanVien);
        }

        // Nếu xe đã được duyệt vào thì cần trả lại chỗ đỗ
        if (transaction.getTrangThai() == TrangThaiGiaoDich.APPROVED_IN) {
            parkingLotService.handleUnparkVehicle(transaction.getParkingLot().getMaBaiDo());
        }

        transaction.cancel();
        return parkingTransactionRepository.save(transaction);
    }

    // === QUERY METHODS ===

    public ParkingTransaction fetchTransactionById(Long maGiaoDich) throws IdInvalidException {
        Optional<ParkingTransaction> transactionOpt = parkingTransactionRepository.findById(maGiaoDich);
        if (transactionOpt.isEmpty()) {
            throw new IdInvalidException("Không tìm thấy giao dịch với mã: " + maGiaoDich);
        }
        return transactionOpt.get();
    }

    public List<ParkingTransaction> fetchAllTransactions() {
        return parkingTransactionRepository.findAll();
    }

    public List<ParkingTransaction> fetchPendingInTransactions() {
        return parkingTransactionRepository.findByTrangThaiOrderByCreatedDateAsc(TrangThaiGiaoDich.PENDING_IN);
    }

    public List<ParkingTransaction> fetchPendingOutTransactions() {
        return parkingTransactionRepository.findPendingOutTransactions();
    }

    public List<ParkingTransaction> fetchTransactionsByBienSoXe(String bienSoXe) {
        return parkingTransactionRepository.findByBienSoXeAndTimeRange(
                bienSoXe,
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now());
    }

    public Optional<ParkingTransaction> fetchActiveTransactionByBienSoXe(String bienSoXe) {
        return parkingTransactionRepository.findActiveTransactionByBienSoXe(bienSoXe);
    }

    public List<ParkingTransaction> fetchCompletedTransactionsByDate(LocalDateTime date) {
        return parkingTransactionRepository.findCompletedTransactionsByDate(date);
    }

    public boolean isVehicleCurrentlyParked(String bienSoXe) {
        return parkingTransactionRepository.countVehicleCurrentlyParked(bienSoXe) > 0;
    }

    // === STATISTICS METHODS ===

    public List<Object[]> getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return parkingTransactionRepository.getRevenueByDateRange(startDate, endDate);
    }

    public List<Object[]> getVehicleCountByTypeAndDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return parkingTransactionRepository.getVehicleCountByTypeAndDateRange(startDate, endDate);
    }

    public Long countActiveTransactionsByParkingLot(String maBaiDo) {
        ParkingLot parkingLot = parkingLotService.fetchParkingLotByMaBaiDo(maBaiDo);
        if (parkingLot == null)
            return 0L;
        return parkingTransactionRepository.countActiveTransactionsByParkingLot(parkingLot);
    }
}
