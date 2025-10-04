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
import nckh.felix.StupidParking.service.FaceRecognitionIntegrationService.FaceRecognitionResult;
import nckh.felix.StupidParking.service.FaceRecognitionIntegrationService.FaceVerificationResult;
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
    private final DangKyThangService dangKyThangService;
    private final FaceRecognitionIntegrationService faceRecognitionService;

    public ParkingTransactionService(ParkingTransactionRepository parkingTransactionRepository,
            ParkingLotService parkingLotService,
            StaffService staffService,
            PriceService priceService,
            VehicleService vehicleService,
            VehicleTypeService vehicleTypeService,
            DangKyThangService dangKyThangService,
            FaceRecognitionIntegrationService faceRecognitionService) {
        this.parkingTransactionRepository = parkingTransactionRepository;
        this.parkingLotService = parkingLotService;
        this.staffService = staffService;
        this.priceService = priceService;
        this.vehicleService = vehicleService;
        this.vehicleTypeService = vehicleTypeService;
        this.dangKyThangService = dangKyThangService;
        this.faceRecognitionService = faceRecognitionService;
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
     * CHO XE VÀO TRỰC TIẾP VỚI FACE RECOGNITION - DÀNH CHO MOBILE/CAMERA SCAN
     * Kết hợp tạo yêu cầu và duyệt vào trong 1 bước + xác thực khuôn mặt
     */
    public ParkingTransaction directVehicleEntryWithFace(String bienSoXe, String maBaiDo, String maLoaiXe,
            String maNhanVien, String ghiChu, String faceImageBase64)
            throws IdInvalidException {
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
            VehicleType vehicleType = vehicleTypeService.fetchVehicleTypeByMaLoaiXe(maLoaiXe);
            if (vehicleType == null) {
                throw new IllegalArgumentException("Không tìm thấy loại xe: " + maLoaiXe);
            }

            vehicle = new Vehicle();
            vehicle.setBienSoXe(bienSoXe);
            vehicle.setMaLoaiXe(vehicleType);
            vehicle.setTenXe("Xe " + maLoaiXe + " - " + bienSoXe);
            vehicle = vehicleService.handleCreateVehicle(vehicle);
        } else {
            if (!vehicle.getMaLoaiXe().getMaLoaiXe().equals(maLoaiXe)) {
                throw new IllegalArgumentException("Xe " + bienSoXe + " là loại " + vehicle.getMaLoaiXe().getMaLoaiXe()
                        + ", không phải " + maLoaiXe);
            }
        }

        // Kiểm tra đăng ký tháng
        boolean hasActiveMonthlyRegistration = dangKyThangService.hasActiveDangKyThang(bienSoXe);
        nckh.felix.StupidParking.domain.DangKyThang activeDangKy = null;

        if (hasActiveMonthlyRegistration) {
            activeDangKy = dangKyThangService.getActiveDangKyThang(bienSoXe);
        }

        // Tạo giao dịch
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setBienSoXe(bienSoXe);
        transaction.setParkingLot(parkingLot);
        transaction.setVehicleType(vehicle.getMaLoaiXe());
        transaction.setThoiGianVao(LocalDateTime.now());
        transaction.setGhiChu(ghiChu);

        // XỬ LÝ FACE RECOGNITION
        if (faceImageBase64 != null && !faceImageBase64.trim().isEmpty()) {
            try {
                if (hasActiveMonthlyRegistration && activeDangKy != null) {
                    // XE CÓ ĐĂNG KÝ THÁNG - NHẬN DIỆN KHUÔN MẶT
                    FaceRecognitionIntegrationService.FaceRecognitionResult faceResult = faceRecognitionService
                            .recognizeFaceForEntry(faceImageBase64, 0.6);

                    if (faceResult.isSuccess()) {
                        // Lưu thông tin face khi xe vào
                        transaction.setFaceIdEntry(faceResult.getFaceId());
                        transaction.setFaceSimilarityEntry(faceResult.getSimilarity());

                        // Xác thực với đăng ký tháng
                        FaceRecognitionIntegrationService.FaceVerificationResult verifyResult = faceRecognitionService
                                .verifyFaceForMonthlyRegistration(
                                        activeDangKy, faceResult.getFaceId(), faceResult.getSimilarity());

                        transaction.setFaceVerificationStatus(verifyResult.getStatus());

                        if (verifyResult.isSuccess()) {
                            String successNote = " [Xe có đăng ký tháng - Xác thực khuôn mặt thành công - Miễn phí]";
                            transaction.setGhiChu((ghiChu != null ? ghiChu : "") + successNote);
                        } else {
                            String warningNote = " [CẢNH BÁO: Khuôn mặt không khớp với đăng ký tháng - "
                                    + verifyResult.getMessage() + "]";
                            transaction.setGhiChu((ghiChu != null ? ghiChu : "") + warningNote);
                        }
                    } else {
                        // Nhận diện thất bại cho xe có đăng ký tháng
                        transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.FAILED_ENTRY);
                        String failNote = " [CẢNH BÁO: Nhận diện khuôn mặt thất bại - " + faceResult.getMessage() + "]";
                        transaction.setGhiChu((ghiChu != null ? ghiChu : "") + failNote);
                    }
                } else {
                    // XE VÃNG LAI - ĐĂNG KÝ KHUÔN MẶT TẠM THỜI
                    FaceRecognitionIntegrationService.FaceRegistrationResult regResult = faceRecognitionService
                            .registerFaceForVisitor(faceImageBase64, bienSoXe);

                    if (regResult.isSuccess()) {
                        // Lưu thông tin face mới tạo
                        transaction.setFaceIdEntry(regResult.getFaceId());
                        transaction.setFaceSimilarityEntry(regResult.getSimilarity());
                        transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.VERIFIED_ENTRY);

                        String visitorNote = " [Xe vãng lai - Đã đăng ký khuôn mặt tạm thời - Face ID: "
                                + regResult.getFaceId() + "]";
                        transaction.setGhiChu((ghiChu != null ? ghiChu : "") + visitorNote);
                    } else {
                        // Đăng ký thất bại cho xe vãng lai
                        transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.FAILED_ENTRY);
                        String failNote = " [CẢNH BÁO: Đăng ký khuôn mặt thất bại - " + regResult.getMessage() + "]";
                        transaction.setGhiChu((ghiChu != null ? ghiChu : "") + failNote);
                    }
                }
            } catch (Exception e) {
                // Lỗi hệ thống face recognition
                transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.FAILED_ENTRY);
                String errorNote = " [LỖI: Hệ thống nhận diện khuôn mặt gặp sự cố - " + e.getMessage() + "]";
                transaction.setGhiChu((ghiChu != null ? ghiChu : "") + errorNote);
            }
        } else {
            // Không có ảnh khuôn mặt - bỏ qua xác thực
            transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.BYPASSED);
            if (hasActiveMonthlyRegistration) {
                String monthlyNote = " [Xe có đăng ký tháng - Bỏ qua xác thực khuôn mặt - Miễn phí]";
                transaction.setGhiChu((ghiChu != null ? ghiChu : "") + monthlyNote);
            }
        }

        // DUYỆT VÀO NGAY LẬP TỨC
        transaction.approveEntry(staff);

        // Lưu giao dịch trước
        transaction = parkingTransactionRepository.save(transaction);

        // Cập nhật số lượng xe trong bãi đỗ
        parkingLotService.handleParkVehicle(parkingLot.getMaBaiDo());

        return transaction;
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

        // KIỂM TRA ĐĂNG KÝ THÁNG
        boolean hasActiveMonthlyRegistration = dangKyThangService.hasActiveDangKyThang(bienSoXe);

        // Tạo giao dịch và CHO VÀO TRỰC TIẾP
        ParkingTransaction transaction = new ParkingTransaction();
        transaction.setBienSoXe(bienSoXe);
        transaction.setParkingLot(parkingLot);
        transaction.setVehicleType(vehicle.getMaLoaiXe());
        transaction.setThoiGianVao(LocalDateTime.now());
        transaction.setGhiChu(ghiChu);

        // Ghi chú nếu xe có đăng ký tháng
        if (hasActiveMonthlyRegistration) {
            String monthlyNote = " [Xe có đăng ký tháng - Miễn phí]";
            transaction.setGhiChu((ghiChu != null ? ghiChu : "") + monthlyNote);
        }

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

        // Kiểm tra đăng ký tháng để áp dụng logic tính phí
        boolean hasActiveMonthlyRegistration = dangKyThangService.hasActiveDangKyThang(transaction.getBienSoXe());

        if (hasActiveMonthlyRegistration) {
            // Xe có đăng ký tháng - Miễn phí
            soTienThanhToan = BigDecimal.ZERO;
            String monthlyNote = " [Xe có đăng ký tháng - Miễn phí]";
            transaction.setGhiChu((transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + monthlyNote);
        }

        // Hoàn thành giao dịch
        transaction.completeTransaction(staff, soTienThanhToan);

        // Cập nhật số lượng xe trong bãi đỗ
        parkingLotService.handleUnparkVehicle(transaction.getParkingLot().getMaBaiDo());

        return parkingTransactionRepository.save(transaction);
    }

    /**
     * CHO XE RA TRỰC TIẾP VỚI FACE RECOGNITION - DÀNH CHO MOBILE/CAMERA SCAN
     * Kết hợp tạo yêu cầu và duyệt ra trong 1 bước + xác thực khuôn mặt
     */
    public ParkingTransaction directVehicleExitWithFace(String bienSoXe, String maNhanVien,
            BigDecimal soTienThanhToan, String faceImageBase64)
            throws IdInvalidException {
        // 1. Kiểm tra xe có đang đỗ trong bãi không
        Optional<ParkingTransaction> activeTransactionOpt = parkingTransactionRepository
                .findActiveTransactionByBienSoXe(bienSoXe);

        if (activeTransactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy xe " + bienSoXe + " trong bãi đỗ");
        }

        ParkingTransaction transaction = activeTransactionOpt.get();

        // 2. Kiểm tra giao dịch đang active
        if (transaction.getTrangThai() != TrangThaiGiaoDich.APPROVED_IN) {
            throw new IllegalStateException("Xe " + bienSoXe + " không ở trạng thái đang đỗ trong bãi");
        }

        // 3. Xác thực nhân viên
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên: " + maNhanVien);
        }

        // 4. Cập nhật thời gian ra
        transaction.setThoiGianRa(LocalDateTime.now());

        // 5. Kiểm tra đăng ký tháng
        boolean hasActiveMonthlyRegistration = dangKyThangService.hasActiveDangKyThang(bienSoXe);
        nckh.felix.StupidParking.domain.DangKyThang activeDangKy = null;

        if (hasActiveMonthlyRegistration) {
            activeDangKy = dangKyThangService.getActiveDangKyThang(bienSoXe);
        }

        // 6. XỬ LÝ FACE RECOGNITION
        if (faceImageBase64 != null && !faceImageBase64.trim().isEmpty()) {
            try {
                // Nhận diện khuôn mặt khi xe ra
                FaceRecognitionIntegrationService.FaceRecognitionResult faceResult = faceRecognitionService
                        .recognizeFaceForExit(faceImageBase64, 0.6);

                if (faceResult.isSuccess()) {
                    // Lưu thông tin face khi xe ra
                    transaction.setFaceIdExit(faceResult.getFaceId());
                    transaction.setFaceSimilarityExit(faceResult.getSimilarity());

                    if (hasActiveMonthlyRegistration && activeDangKy != null) {
                        // Xe có đăng ký tháng - xác thực với đăng ký tháng
                        FaceRecognitionIntegrationService.FaceVerificationResult verifyResult = faceRecognitionService
                                .verifyFaceForMonthlyRegistration(
                                        activeDangKy, faceResult.getFaceId(), faceResult.getSimilarity());

                        if (verifyResult.isSuccess()) {
                            // Cập nhật status cho cả xe vào và xe ra đều OK
                            if (transaction
                                    .getFaceVerificationStatus() == ParkingTransaction.FaceVerificationStatus.VERIFIED_ENTRY) {
                                transaction.setFaceVerificationStatus(
                                        ParkingTransaction.FaceVerificationStatus.VERIFIED_BOTH);
                            } else {
                                transaction.setFaceVerificationStatus(
                                        ParkingTransaction.FaceVerificationStatus.VERIFIED_EXIT);
                            }

                            soTienThanhToan = BigDecimal.ZERO; // Miễn phí cho xe có đăng ký tháng
                            String successNote = " [Xe ra - Xác thực khuôn mặt thành công - Miễn phí]";
                            transaction.setGhiChu(
                                    (transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + successNote);
                        } else {
                            transaction
                                    .setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
                            String warningNote = " [CẢNH BÁO XE RA: Khuôn mặt không khớp với đăng ký tháng - "
                                    + verifyResult.getMessage() + "]";
                            transaction.setGhiChu(
                                    (transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + warningNote);
                        }
                    } else {
                        // XE VÃNG LAI - SO SÁNH FACE VÀO VÀ RA
                        if (transaction.getFaceIdEntry() != null) {
                            FaceRecognitionIntegrationService.FaceVerificationResult verifyResult = faceRecognitionService
                                    .verifyFaceForVisitorExit(faceImageBase64, transaction.getFaceIdEntry(), 0.6);

                            // Lưu face_id_exit từ kết quả nhận diện
                            transaction.setFaceIdExit(faceResult.getFaceId());
                            transaction.setFaceSimilarityExit(faceResult.getSimilarity());

                            if (verifyResult.isSuccess()) {
                                // Khuôn mặt khớp - cho ra
                                transaction.setFaceVerificationStatus(
                                        ParkingTransaction.FaceVerificationStatus.VERIFIED_BOTH);
                                String successNote = " [Xe vãng lai ra - Xác thực khuôn mặt thành công - Khuôn mặt khớp với lúc vào]";
                                transaction.setGhiChu(
                                        (transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + successNote);
                            } else {
                                // Khuôn mặt không khớp - cảnh báo
                                transaction.setFaceVerificationStatus(
                                        ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
                                String warningNote = " [CẢNH BÁO XE RA: Khuôn mặt xe ra không khớp với xe vào - "
                                        + verifyResult.getMessage() + "]";
                                transaction.setGhiChu(
                                        (transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + warningNote);
                            }
                        } else {
                            // Không có face_id_entry để so sánh
                            transaction.setFaceIdExit(faceResult.getFaceId());
                            transaction.setFaceSimilarityExit(faceResult.getSimilarity());
                            transaction.setFaceVerificationStatus(
                                    ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
                            String warningNote = " [CẢNH BÁO XE RA: Không có thông tin khuôn mặt khi xe vào để so sánh]";
                            transaction.setGhiChu(
                                    (transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + warningNote);
                        }
                    }
                } else {
                    // Nhận diện thất bại
                    transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
                    String failNote = " [CẢNH BÁO XE RA: Nhận diện khuôn mặt thất bại - " + faceResult.getMessage()
                            + "]";
                    transaction.setGhiChu((transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + failNote);
                }
            } catch (Exception e) {
                // Lỗi hệ thống face recognition
                transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.FAILED_EXIT);
                String errorNote = " [LỖI XE RA: Hệ thống nhận diện khuôn mặt gặp sự cố - " + e.getMessage() + "]";
                transaction.setGhiChu((transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + errorNote);
            }
        } else {
            // Không có ảnh khuôn mặt - bỏ qua xác thực
            if (transaction.getFaceVerificationStatus() != ParkingTransaction.FaceVerificationStatus.BYPASSED) {
                transaction.setFaceVerificationStatus(ParkingTransaction.FaceVerificationStatus.BYPASSED);
            }

            if (hasActiveMonthlyRegistration) {
                soTienThanhToan = BigDecimal.ZERO; // Miễn phí cho xe có đăng ký tháng
                String monthlyNote = " [Xe ra - Bỏ qua xác thực khuôn mặt - Miễn phí]";
                transaction.setGhiChu((transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + monthlyNote);
            }
        }

        // 7. Xử lý tính phí cho xe vãng lai
        if (!hasActiveMonthlyRegistration) {
            if (soTienThanhToan == null) {
                long hours = transaction.getParkingDurationInHours();
                BigDecimal hourlyRate = priceService.getHourlyRateByVehicleType(
                        transaction.getVehicleType().getMaLoaiXe());
                soTienThanhToan = hourlyRate.multiply(BigDecimal.valueOf(hours));
            }
        }

        // 8. Hoàn thành giao dịch và ghi nhận nhân viên + số tiền
        transaction.completeTransaction(staff, soTienThanhToan);

        // 9. Cập nhật số chỗ trống trong bãi đỗ
        parkingLotService.handleUnparkVehicle(transaction.getParkingLot().getMaBaiDo());

        return parkingTransactionRepository.save(transaction);
    }

    /**
     * CHO XE RA TRỰC TIẾP - DÀNH CHO MOBILE/CAMERA SCAN
     * Kết hợp tạo yêu cầu và duyệt ra trong 1 bước
     */
    public ParkingTransaction directVehicleExit(String bienSoXe, String maNhanVien, BigDecimal soTienThanhToan)
            throws IdInvalidException {
        // 1. Kiểm tra xe có đang đỗ trong bãi không
        Optional<ParkingTransaction> activeTransactionOpt = parkingTransactionRepository
                .findActiveTransactionByBienSoXe(bienSoXe);

        if (activeTransactionOpt.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy xe " + bienSoXe + " trong bãi đỗ");
        }

        ParkingTransaction transaction = activeTransactionOpt.get();

        // 2. Kiểm tra giao dịch đang active
        if (transaction.getTrangThai() != TrangThaiGiaoDich.APPROVED_IN) {
            throw new IllegalStateException("Xe " + bienSoXe + " không ở trạng thái đang đỗ trong bãi");
        }

        // 3. Xác thực nhân viên
        Staff staff = staffService.fetchStaffByMaNV(maNhanVien);
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên: " + maNhanVien);
        }

        // 4. Cập nhật thời gian ra
        transaction.setThoiGianRa(LocalDateTime.now());

        // 5. Kiểm tra đăng ký tháng để áp dụng logic tính phí
        boolean hasActiveMonthlyRegistration = dangKyThangService.hasActiveDangKyThang(bienSoXe);

        if (hasActiveMonthlyRegistration) {
            // Xe có đăng ký tháng - Miễn phí
            soTienThanhToan = BigDecimal.ZERO;
            String monthlyNote = " [Xe có đăng ký tháng - Miễn phí]";
            transaction.setGhiChu((transaction.getGhiChu() != null ? transaction.getGhiChu() : "") + monthlyNote);
        } else {
            // Xe vãng lai - Tính phí bình thường
            if (soTienThanhToan == null) {
                long hours = transaction.getParkingDurationInHours();
                // Lấy giá từ bảng Price theo loại xe
                BigDecimal hourlyRate = priceService.getHourlyRateByVehicleType(
                        transaction.getVehicleType().getMaLoaiXe());
                soTienThanhToan = hourlyRate.multiply(BigDecimal.valueOf(hours));
            }
        }

        // 6. Hoàn thành giao dịch và ghi nhận nhân viên + số tiền
        transaction.completeTransaction(staff, soTienThanhToan);

        // 7. Cập nhật số chỗ trống trong bãi đỗ
        parkingLotService.handleUnparkVehicle(transaction.getParkingLot().getMaBaiDo());

        return parkingTransactionRepository.save(transaction);
    }

    /**
     * Tính tiền đỗ xe tự động
     */
    public BigDecimal calculateParkingFee(Long maGiaoDich) throws IdInvalidException {
        ParkingTransaction transaction = fetchTransactionById(maGiaoDich);

        long hours = transaction.getParkingDurationInHours();
        // Không cần check tối thiểu 1 giờ nữa vì đã xử lý trong
        // getParkingDurationInHours()

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

    // === MONTHLY REGISTRATION METHODS ===

    /**
     * Kiểm tra trạng thái đăng ký tháng của xe khi quét biển số
     * 
     * @param bienSoXe Biển số xe cần kiểm tra
     * @return Thông tin về trạng thái đăng ký tháng và khả năng vào bãi
     */
    public VehicleEntryStatus checkVehicleEntryStatus(String bienSoXe) {
        // Kiểm tra xe có đang đỗ trong bãi không
        boolean isCurrentlyParked = isVehicleCurrentlyParked(bienSoXe);

        // Kiểm tra đăng ký tháng
        boolean hasActiveMonthlyRegistration = dangKyThangService.hasActiveDangKyThang(bienSoXe);

        return new VehicleEntryStatus(bienSoXe, hasActiveMonthlyRegistration, isCurrentlyParked);
    }

    /**
     * Class để trả về thông tin trạng thái xe
     */
    public static class VehicleEntryStatus {
        private String bienSoXe;
        private boolean hasActiveMonthlyRegistration;
        private boolean isCurrentlyParked;
        private String message;

        public VehicleEntryStatus(String bienSoXe, boolean hasActiveMonthlyRegistration, boolean isCurrentlyParked) {
            this.bienSoXe = bienSoXe;
            this.hasActiveMonthlyRegistration = hasActiveMonthlyRegistration;
            this.isCurrentlyParked = isCurrentlyParked;
            generateMessage();
        }

        private void generateMessage() {
            if (isCurrentlyParked) {
                message = "Xe đang đỗ trong bãi";
            } else if (hasActiveMonthlyRegistration) {
                message = "Xe có đăng ký tháng còn hiệu lực - Cho vào miễn phí";
            } else {
                message = "Xe vãng lai - Áp dụng tính phí theo giờ";
            }
        }

        // Getters and setters
        public String getBienSoXe() {
            return bienSoXe;
        }

        public void setBienSoXe(String bienSoXe) {
            this.bienSoXe = bienSoXe;
        }

        public boolean isHasActiveMonthlyRegistration() {
            return hasActiveMonthlyRegistration;
        }

        public void setHasActiveMonthlyRegistration(boolean hasActiveMonthlyRegistration) {
            this.hasActiveMonthlyRegistration = hasActiveMonthlyRegistration;
        }

        public boolean isCurrentlyParked() {
            return isCurrentlyParked;
        }

        public void setCurrentlyParked(boolean currentlyParked) {
            isCurrentlyParked = currentlyParked;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public boolean canEnter() {
            return !isCurrentlyParked;
        }
    }
}
