package nckh.felix.StupidParking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import nckh.felix.StupidParking.domain.DangKyThang;
import nckh.felix.StupidParking.domain.dto.DangKyThangCreateDTO;
import nckh.felix.StupidParking.repository.DangKyThangRepository;

@Service
public class DangKyThangValidationService {

    private final DangKyThangRepository dangKyThangRepository;

    public DangKyThangValidationService(DangKyThangRepository dangKyThangRepository) {
        this.dangKyThangRepository = dangKyThangRepository;
    }

    /**
     * Validate toàn diện cho việc tạo đăng ký tháng mới
     * 
     * @param dto DTO chứa thông tin đăng ký tháng
     * @throws IllegalArgumentException nếu có lỗi validation
     */
    public void validateCreateDangKyThang(DangKyThangCreateDTO dto) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Kiểm tra CCCD đã có đăng ký tháng active chưa
        validateCccdUniqueness(dto.getCccd(), now);

        // 2. Kiểm tra số cavet đã có đăng ký tháng active chưa
        validateSoCavetUniqueness(dto.getSoCavet(), now);

        // 3. Kiểm tra biển số xe đã có đăng ký tháng active chưa
        validateBienSoXeUniqueness(dto.getBienSoXe(), now);

        // 4. Kiểm tra logic nghiệp vụ
        validateBusinessLogic(dto);
    }

    /**
     * Kiểm tra CCCD không được trùng với đăng ký tháng active khác
     */
    private void validateCccdUniqueness(String cccd, LocalDateTime currentTime) {
        if (dangKyThangRepository.existsActiveDangKyThangByCccd(cccd, currentTime)) {
            List<DangKyThang> activeDangKy = dangKyThangRepository.findActiveDangKyThangByCccd(cccd, currentTime);
            DangKyThang existingDangKy = activeDangKy.get(0);

            throw new IllegalArgumentException(
                    String.format("CCCD '%s' đã có đăng ký tháng còn hiệu lực cho xe '%s' (hết hạn: %s). " +
                            "Một CCCD chỉ có thể có 1 đăng ký tháng hiệu lực tại một thời điểm.",
                            cccd,
                            existingDangKy.getBienSoXe(),
                            existingDangKy.getThoiGianHetHan().toLocalDate()));
        }
    }

    /**
     * Kiểm tra số cavet không được trùng với đăng ký tháng active khác
     */
    private void validateSoCavetUniqueness(String soCavet, LocalDateTime currentTime) {
        if (dangKyThangRepository.existsActiveDangKyThangBySoCavet(soCavet, currentTime)) {
            List<DangKyThang> activeDangKy = dangKyThangRepository.findActiveDangKyThangBySoCavet(soCavet, currentTime);
            DangKyThang existingDangKy = activeDangKy.get(0);

            throw new IllegalArgumentException(
                    String.format("Số cavet '%s' đã có đăng ký tháng còn hiệu lực cho xe '%s' (hết hạn: %s). " +
                            "Một số cavet chỉ có thể có 1 đăng ký tháng hiệu lực tại một thời điểm.",
                            soCavet,
                            existingDangKy.getBienSoXe(),
                            existingDangKy.getThoiGianHetHan().toLocalDate()));
        }
    }

    /**
     * Kiểm tra biển số xe không được trùng với đăng ký tháng active khác
     */
    private void validateBienSoXeUniqueness(String bienSoXe, LocalDateTime currentTime) {
        if (dangKyThangRepository.existsActiveDangKyThangByBienSoXe(bienSoXe, currentTime)) {
            throw new IllegalArgumentException(
                    String.format("Biển số xe '%s' đã có đăng ký tháng còn hiệu lực. " +
                            "Một xe chỉ có thể có 1 đăng ký tháng hiệu lực tại một thời điểm. " +
                            "Vui lòng kiểm tra lại hoặc hủy đăng ký cũ trước khi tạo mới.",
                            bienSoXe));
        }
    }

    /**
     * Kiểm tra các logic nghiệp vụ khác
     */
    private void validateBusinessLogic(DangKyThangCreateDTO dto) {
        // 1. Số tháng hợp lệ (đã có @Min @Max trong entity)
        if (dto.getSoThang() == null || dto.getSoThang() < 1 || dto.getSoThang() > 12) {
            throw new IllegalArgumentException("Số tháng đăng ký phải từ 1 đến 12");
        }

        // 2. CCCD hợp lệ
        if (dto.getCccd() == null || !dto.getCccd().matches("^[0-9]{12}$")) {
            throw new IllegalArgumentException("CCCD phải có đúng 12 chữ số");
        }

        // 3. Biển số xe không được rỗng
        if (dto.getBienSoXe() == null || dto.getBienSoXe().trim().isEmpty()) {
            throw new IllegalArgumentException("Biển số xe không được để trống");
        }

        // 4. Số cavet không được rỗng
        if (dto.getSoCavet() == null || dto.getSoCavet().trim().isEmpty()) {
            throw new IllegalArgumentException("Số cavet xe không được để trống");
        }

        // 5. Địa chỉ không được rỗng
        if (dto.getDiaChi() == null || dto.getDiaChi().trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ cư trú không được để trống");
        }

        // 6. Loại xe phải hợp lệ
        if (dto.getMaLoaiXe() == null || dto.getMaLoaiXe().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã loại xe không được để trống");
        }

        // 7. Nhân viên tạo phải hợp lệ
        if (dto.getMaNhanVien() == null || dto.getMaNhanVien().trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhân viên tạo không được để trống");
        }
    }

    /**
     * Kiểm tra có thể gia hạn đăng ký tháng không
     */
    public boolean canExtendDangKyThang(String bienSoXe, LocalDateTime currentTime) {
        return dangKyThangRepository.existsActiveDangKyThangByBienSoXe(bienSoXe, currentTime);
    }

    /**
     * Lấy thông tin đăng ký tháng active của xe
     */
    public DangKyThang getActiveDangKyThang(String bienSoXe, LocalDateTime currentTime) {
        return dangKyThangRepository.findActiveDangKyThangByBienSoXe(bienSoXe, currentTime).orElse(null);
    }
}
